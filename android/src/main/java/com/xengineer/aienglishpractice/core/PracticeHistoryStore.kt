package com.xengineer.aienglishpractice.core

import java.io.StringReader
import java.io.StringWriter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Properties

data class PracticeHistoryEntry(
    val id: String,
    val scenarioId: String,
    val scenarioName: String,
    val completedAtLabel: String,
    val turnCount: Int,
    val averageScore: Int,
    val strengths: List<String>,
    val improvements: List<String>,
    val nextGoal: String,
    val durationLabel: String = "",
    val completedAtEpochDay: Long = 0L,
    val scoreBreakdown: List<SummaryScoreBreakdown> = emptyList(),
    val completedAtEpochMillis: Long = 0L
) {
    fun toSummary(): PracticeSummary = PracticeSummary(
        turnCount = turnCount,
        averageScore = averageScore,
        strengths = strengths,
        improvements = improvements,
        nextGoal = nextGoal,
        scoreBreakdown = scoreBreakdown.ifEmpty {
            fallbackScoreBreakdown(averageScore)
        }
    )

    fun completedAtDisplayLabel(nowMillis: Long = currentEpochMillis()): String =
        if (completedAtEpochMillis > 0L) {
            completedAtLabelFor(completedAtEpochMillis, completedAtEpochDay, nowMillis)
        } else {
            completedAtLabel.ifBlank { completedAtLabelFor(0L, completedAtEpochDay, nowMillis) }
        }

    companion object {
        fun fromSummary(
            scenario: PracticeScenario,
            summary: PracticeSummary,
            durationLabel: String = "",
            completedAtEpochDay: Long = UnknownEpochDay,
            completedAtEpochMillis: Long = UnknownEpochMillis,
            completedAtLabel: String = "",
            id: String = ""
        ): PracticeHistoryEntry {
            val resolvedMillis = resolveCompletedAtMillis(completedAtEpochMillis, completedAtEpochDay)
            val resolvedEpochDay = resolveCompletedAtEpochDay(completedAtEpochDay, resolvedMillis)
            return PracticeHistoryEntry(
                id = id.ifBlank { "${scenario.id}-$resolvedMillis" },
                scenarioId = scenario.id,
                scenarioName = scenario.name,
                completedAtLabel = completedAtLabel.ifBlank {
                    completedAtLabelFor(resolvedMillis, resolvedEpochDay)
                },
                turnCount = summary.turnCount,
                averageScore = summary.averageScore,
                strengths = summary.strengths,
                improvements = summary.improvements,
                nextGoal = summary.nextGoal,
                durationLabel = durationLabel,
                completedAtEpochDay = resolvedEpochDay,
                scoreBreakdown = summary.scoreBreakdown,
                completedAtEpochMillis = resolvedMillis
            )
        }
    }
}

private fun fallbackScoreBreakdown(score: Int): List<SummaryScoreBreakdown> {
    val safeScore = score.coerceIn(0, 100)
    return listOf(
        SummaryScoreBreakdown("语法", safeScore, "历史记录未保存单项分数。"),
        SummaryScoreBreakdown("流利度", safeScore, "历史记录未保存单项分数。"),
        SummaryScoreBreakdown("发音", safeScore, "历史记录未保存单项分数。"),
        SummaryScoreBreakdown("完成度", safeScore, "历史记录未保存单项分数。"),
        SummaryScoreBreakdown("词汇", safeScore, "历史记录未保存单项分数。")
    )
}

interface PracticeHistoryStorage {
    fun read(): String?

    fun write(value: String)

    fun clear()
}

class PracticeHistoryStore(private val storage: PracticeHistoryStorage? = null) {
    private val entries = PracticeHistoryCodec.decode(storage?.read()).toMutableList()

    fun record(entry: PracticeHistoryEntry) {
        entries.removeAll { existing -> existing.id == entry.id }
        entries.add(0, entry)
        persist()
    }

    fun recent(limit: Int = 10): List<PracticeHistoryEntry> = entries.take(limit)

    fun latest(): PracticeHistoryEntry? = entries.firstOrNull()

    fun totalTurns(): Int = entries.sumOf { entry -> entry.turnCount }

    fun completedTodayCount(todayEpochDay: Long = currentEpochDay()): Int =
        entries.count { entry -> entry.completedAtEpochDay == todayEpochDay }

    fun clear() {
        entries.clear()
        storage?.clear()
    }

    private fun persist() {
        storage?.write(PracticeHistoryCodec.encode(entries))
    }
}

object LocalPracticeHistory {
    val store = PracticeHistoryStore()
}

private object PracticeHistoryCodec {
    fun encode(entries: List<PracticeHistoryEntry>): String {
        val properties = Properties()
        properties.setProperty("version", "1")
        properties.setProperty("count", entries.size.toString())
        entries.forEachIndexed { index, entry ->
            val prefix = "entry.$index"
            properties.setProperty("$prefix.id", entry.id)
            properties.setProperty("$prefix.scenarioId", entry.scenarioId)
            properties.setProperty("$prefix.scenarioName", entry.scenarioName)
            properties.setProperty("$prefix.completedAtLabel", entry.completedAtLabel)
            properties.setProperty("$prefix.turnCount", entry.turnCount.toString())
            properties.setProperty("$prefix.averageScore", entry.averageScore.toString())
            properties.setProperty("$prefix.nextGoal", entry.nextGoal)
            properties.setProperty("$prefix.durationLabel", entry.durationLabel)
            properties.setProperty("$prefix.completedAtEpochDay", entry.completedAtEpochDay.toString())
            properties.setProperty("$prefix.completedAtEpochMillis", entry.completedAtEpochMillis.toString())
            properties.setStringList("$prefix.strengths", entry.strengths)
            properties.setStringList("$prefix.improvements", entry.improvements)
            properties.setScoreBreakdown("$prefix.scoreBreakdown", entry.scoreBreakdown)
        }

        return StringWriter().use { writer ->
            properties.store(writer, null)
            writer.toString()
        }
    }

    fun decode(rawValue: String?): List<PracticeHistoryEntry> {
        if (rawValue.isNullOrBlank()) return emptyList()

        return try {
            val properties = Properties().apply {
                load(StringReader(rawValue))
            }
            val count = properties.getProperty("count")?.toIntOrNull() ?: return emptyList()
            (0 until count).map { index ->
                val prefix = "entry.$index"
                val completedAtEpochMillis = properties.getProperty("$prefix.completedAtEpochMillis")?.toLongOrNull() ?: 0L
                val completedAtEpochDay = properties.getProperty("$prefix.completedAtEpochDay")?.toLongOrNull()
                    ?: if (completedAtEpochMillis > 0L) epochDayFromMillis(completedAtEpochMillis) else currentEpochDay()
                val storedCompletedAtLabel = properties.getProperty("$prefix.completedAtLabel").orEmpty()
                PracticeHistoryEntry(
                    id = properties.getProperty("$prefix.id").orEmpty(),
                    scenarioId = properties.getProperty("$prefix.scenarioId").orEmpty(),
                    scenarioName = properties.getProperty("$prefix.scenarioName").orEmpty(),
                    completedAtLabel = restoredCompletedAtLabel(
                        storedCompletedAtLabel,
                        completedAtEpochMillis,
                        completedAtEpochDay
                    ),
                    turnCount = properties.getProperty("$prefix.turnCount")?.toIntOrNull() ?: 0,
                    averageScore = properties.getProperty("$prefix.averageScore")?.toIntOrNull() ?: 0,
                    strengths = properties.stringList("$prefix.strengths"),
                    improvements = properties.stringList("$prefix.improvements"),
                    nextGoal = properties.getProperty("$prefix.nextGoal").orEmpty(),
                    durationLabel = properties.getProperty("$prefix.durationLabel").orEmpty(),
                    completedAtEpochDay = completedAtEpochDay,
                    scoreBreakdown = properties.scoreBreakdown("$prefix.scoreBreakdown"),
                    completedAtEpochMillis = completedAtEpochMillis
                )
            }
        } catch (_: IllegalArgumentException) {
            emptyList()
        }
    }

    private fun Properties.setStringList(key: String, values: List<String>) {
        setProperty("$key.count", values.size.toString())
        values.forEachIndexed { index, value ->
            setProperty("$key.$index", value)
        }
    }

    private fun Properties.stringList(key: String): List<String> {
        val count = getProperty("$key.count")?.toIntOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until count) {
                getProperty("$key.$index")?.let { value ->
                    add(value)
                }
            }
        }
    }

    private fun Properties.setScoreBreakdown(key: String, values: List<SummaryScoreBreakdown>) {
        setProperty("$key.count", values.size.toString())
        values.forEachIndexed { index, value ->
            setProperty("$key.$index.label", value.label)
            setProperty("$key.$index.score", value.score.toString())
            setProperty("$key.$index.reason", value.reason)
        }
    }

    private fun Properties.scoreBreakdown(key: String): List<SummaryScoreBreakdown> {
        val count = getProperty("$key.count")?.toIntOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until count) {
                val label = getProperty("$key.$index.label") ?: return@buildList
                val score = getProperty("$key.$index.score")?.toIntOrNull() ?: return@buildList
                val reason = getProperty("$key.$index.reason").orEmpty()
                add(SummaryScoreBreakdown(label, score, reason))
            }
        }
    }
}

private const val UnknownEpochDay = Long.MIN_VALUE
private const val UnknownEpochMillis = Long.MIN_VALUE

private val timeLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM月dd日")

private fun restoredCompletedAtLabel(
    storedLabel: String,
    completedAtEpochMillis: Long,
    completedAtEpochDay: Long
): String = when {
    completedAtEpochMillis > 0L -> completedAtLabelFor(completedAtEpochMillis, completedAtEpochDay)
    storedLabel.isBlank() || storedLabel == "刚刚" -> completedAtLabelFor(0L, completedAtEpochDay)
    else -> storedLabel
}

private fun resolveCompletedAtMillis(completedAtEpochMillis: Long, completedAtEpochDay: Long): Long = when {
    completedAtEpochMillis != UnknownEpochMillis -> completedAtEpochMillis
    completedAtEpochDay != UnknownEpochDay && completedAtEpochDay != currentEpochDay() -> startOfDayMillis(completedAtEpochDay)
    else -> currentEpochMillis()
}

private fun resolveCompletedAtEpochDay(completedAtEpochDay: Long, completedAtEpochMillis: Long): Long =
    if (completedAtEpochDay != UnknownEpochDay) completedAtEpochDay else epochDayFromMillis(completedAtEpochMillis)

private fun completedAtLabelFor(
    epochMillis: Long,
    epochDay: Long,
    nowMillis: Long = currentEpochMillis()
): String {
    val displayEpochDay = if (epochDay > 0L) epochDay else epochDayFromMillis(epochMillis)
    val dayDiff = epochDayFromMillis(nowMillis) - displayEpochDay
    return when {
        dayDiff <= 0L -> todayCompletedAtLabel(epochMillis, nowMillis)
        dayDiff == 1L -> "昨天"
        dayDiff in 2L..6L -> "$dayDiff 天前"
        else -> instantForLabel(epochMillis, displayEpochDay).format(dateLabelFormatter)
    }
}

private fun todayCompletedAtLabel(epochMillis: Long, nowMillis: Long): String {
    if (epochMillis <= 0L) return "刚刚"
    val minutes = Duration.between(
        Instant.ofEpochMilli(epochMillis),
        Instant.ofEpochMilli(nowMillis)
    ).toMinutes().coerceAtLeast(0L)
    return when {
        minutes < 1L -> "刚刚"
        minutes < 60L -> "$minutes 分钟前"
        else -> instantForLabel(epochMillis, epochDayFromMillis(epochMillis)).format(timeLabelFormatter).let { "今天 $it" }
    }
}

private fun instantForLabel(epochMillis: Long, epochDay: Long) =
    Instant.ofEpochMilli(if (epochMillis > 0L) epochMillis else startOfDayMillis(epochDay)).atZone(ZoneId.systemDefault())

private fun startOfDayMillis(epochDay: Long): Long =
    LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun epochDayFromMillis(epochMillis: Long): Long =
    Instant.ofEpochMilli(epochMillis.coerceAtLeast(0L)).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

private fun currentEpochMillis(): Long = System.currentTimeMillis()

private fun currentEpochDay(): Long = LocalDate.now().toEpochDay()
