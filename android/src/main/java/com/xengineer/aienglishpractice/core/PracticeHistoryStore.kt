package com.xengineer.aienglishpractice.core

import java.io.StringReader
import java.io.StringWriter
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
    val nextGoal: String
) {
    fun toSummary(): PracticeSummary = PracticeSummary(
        turnCount = turnCount,
        averageScore = averageScore,
        strengths = strengths,
        improvements = improvements,
        nextGoal = nextGoal
    )

    companion object {
        fun fromSummary(
            scenario: PracticeScenario,
            summary: PracticeSummary,
            completedAtLabel: String = "刚刚",
            id: String = "${scenario.id}-${System.currentTimeMillis()}"
        ): PracticeHistoryEntry = PracticeHistoryEntry(
            id = id,
            scenarioId = scenario.id,
            scenarioName = scenario.name,
            completedAtLabel = completedAtLabel,
            turnCount = summary.turnCount,
            averageScore = summary.averageScore,
            strengths = summary.strengths,
            improvements = summary.improvements,
            nextGoal = summary.nextGoal
        )
    }
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
            properties.setStringList("$prefix.strengths", entry.strengths)
            properties.setStringList("$prefix.improvements", entry.improvements)
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
                PracticeHistoryEntry(
                    id = properties.getProperty("$prefix.id").orEmpty(),
                    scenarioId = properties.getProperty("$prefix.scenarioId").orEmpty(),
                    scenarioName = properties.getProperty("$prefix.scenarioName").orEmpty(),
                    completedAtLabel = properties.getProperty("$prefix.completedAtLabel").orEmpty(),
                    turnCount = properties.getProperty("$prefix.turnCount")?.toIntOrNull() ?: 0,
                    averageScore = properties.getProperty("$prefix.averageScore")?.toIntOrNull() ?: 0,
                    strengths = properties.stringList("$prefix.strengths"),
                    improvements = properties.stringList("$prefix.improvements"),
                    nextGoal = properties.getProperty("$prefix.nextGoal").orEmpty()
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
}
