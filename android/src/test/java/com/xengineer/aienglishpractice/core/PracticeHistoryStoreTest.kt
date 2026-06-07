package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PracticeHistoryStoreTest {
    @Test
    fun recordStoresLatestPracticeEntryFirst() {
        val store = PracticeHistoryStore()
        val restaurant = historyEntry(
            id = "entry-restaurant",
            scenario = PracticeScenario.restaurant(),
            averageScore = 82
        )
        val meeting = historyEntry(
            id = "entry-meeting",
            scenario = PracticeScenario.meeting(),
            averageScore = 88
        )

        store.record(restaurant)
        store.record(meeting)

        assertEquals(listOf("entry-meeting", "entry-restaurant"), store.recent().map { it.id })
        assertEquals(meeting, store.latest())
    }

    @Test
    fun entryFromSummaryPreservesSummaryFieldsForHistoryPage() {
        val scenario = PracticeScenario.interview()
        val summary = summary(averageScore = 91, nextGoal = "下次练习更有说服力的项目案例。")

        val entry = PracticeHistoryEntry.fromSummary(
            id = "entry-interview",
            scenario = scenario,
            summary = summary,
            completedAtLabel = "今天 10:00",
            durationLabel = "03:25"
        )

        assertEquals("entry-interview", entry.id)
        assertEquals("interview", entry.scenarioId)
        assertEquals("面试练习", entry.scenarioName)
        assertEquals("今天 10:00", entry.completedAtLabel)
        assertEquals("03:25", entry.durationLabel)
        assertTrue(entry.completedAtEpochDay > 0L)
        assertEquals(91, entry.averageScore)
        assertEquals(summary, entry.toSummary())
        assertEquals(listOf("语法", "流利度", "发音", "完成度", "词汇"), entry.toSummary().scoreBreakdown.map { it.label })
    }

    @Test
    fun entryFromSummaryBuildsDateLabelFromCompletedDayWhenNoLabelIsProvided() {
        val yesterdayEpochDay = LocalDate.now().minusDays(1).toEpochDay()

        val entry = PracticeHistoryEntry.fromSummary(
            id = "entry-yesterday",
            scenario = PracticeScenario.restaurant(),
            summary = summary(),
            completedAtEpochDay = yesterdayEpochDay
        )

        assertEquals("昨天", entry.completedAtLabel)
    }

    @Test
    fun oldHistoryEntryWithoutBreakdownGetsFiveRadarFallbackScores() {
        val entry = PracticeHistoryEntry(
            id = "legacy",
            scenarioId = "restaurant",
            scenarioName = "餐厅点餐",
            completedAtLabel = "刚刚",
            turnCount = 1,
            averageScore = 76,
            strengths = listOf("已完成练习。"),
            improvements = listOf("继续练习。"),
            nextGoal = "重复同一场景。"
        )

        val summary = entry.toSummary()

        assertEquals(5, summary.scoreBreakdown.size)
        assertEquals(listOf("语法", "流利度", "发音", "完成度", "词汇"), summary.scoreBreakdown.map { it.label })
        assertEquals(listOf(76, 76, 76, 76, 76), summary.scoreBreakdown.map { it.score })
    }

    @Test
    fun clearRemovesLocalHistory() {
        val store = PracticeHistoryStore()

        store.record(historyEntry(id = "entry-1", scenario = PracticeScenario.restaurant()))
        store.clear()

        assertTrue(store.recent().isEmpty())
        assertEquals(null, store.latest())
    }

    @Test
    fun recordPersistsHistoryAcrossStoreInstances() {
        val storage = FakePracticeHistoryStorage()
        val entry = historyEntry(id = "entry-persisted", scenario = PracticeScenario.restaurant(), durationLabel = "02:18")

        PracticeHistoryStore(storage).record(entry)
        val restoredStore = PracticeHistoryStore(storage)

        assertEquals(listOf(entry), restoredStore.recent())
        assertEquals("02:18", restoredStore.latest()?.durationLabel)
        assertEquals(entry.completedAtEpochDay, restoredStore.latest()?.completedAtEpochDay)
        assertTrue(storage.savedValue?.contains("entry-persisted") == true)
    }

    @Test
    fun completedTodayCountOnlyCountsEntriesFromGivenDay() {
        val store = PracticeHistoryStore()
        store.record(historyEntry(id = "yesterday", scenario = PracticeScenario.restaurant(), completedAtEpochDay = 100L))
        store.record(historyEntry(id = "today-1", scenario = PracticeScenario.restaurant(), completedAtEpochDay = 101L))
        store.record(historyEntry(id = "today-2", scenario = PracticeScenario.meeting(), completedAtEpochDay = 101L))

        assertEquals(2, store.completedTodayCount(todayEpochDay = 101L))
        assertEquals(1, store.completedTodayCount(todayEpochDay = 100L))
    }

    @Test
    fun streakDaysCountsConsecutivePracticeDays() {
        val store = PracticeHistoryStore()
        store.record(historyEntry(id = "day-8", scenario = PracticeScenario.restaurant(), completedAtEpochDay = 8L))
        store.record(historyEntry(id = "day-10-a", scenario = PracticeScenario.restaurant(), completedAtEpochDay = 10L))
        store.record(historyEntry(id = "day-10-b", scenario = PracticeScenario.meeting(), completedAtEpochDay = 10L))
        store.record(historyEntry(id = "day-11", scenario = PracticeScenario.restaurant(), completedAtEpochDay = 11L))
        store.record(historyEntry(id = "day-12", scenario = PracticeScenario.interview(), completedAtEpochDay = 12L))

        assertEquals(3, store.streakDays(todayEpochDay = 12L))
        assertEquals(3, store.streakDays(todayEpochDay = 13L))
        assertEquals(0, store.streakDays(todayEpochDay = 14L))
    }

    @Test
    fun clearRemovesPersistedHistory() {
        val storage = FakePracticeHistoryStorage()
        PracticeHistoryStore(storage).record(historyEntry(id = "entry-1", scenario = PracticeScenario.restaurant()))

        PracticeHistoryStore(storage).clear()
        val restoredStore = PracticeHistoryStore(storage)

        assertTrue(restoredStore.recent().isEmpty())
        assertEquals(null, storage.savedValue)
    }

    @Test
    fun invalidPersistedHistoryFallsBackToEmptyHistory() {
        val storage = FakePracticeHistoryStorage(initialValue = "\\u")

        val store = PracticeHistoryStore(storage)

        assertTrue(store.recent().isEmpty())
        assertEquals(null, store.latest())
    }

    @Test
    fun homeDashboardUsesLatestLocalHistory() {
        val store = PracticeHistoryStore()
        store.record(historyEntry(id = "entry-1", scenario = PracticeScenario.restaurant(), turnCount = 1))
        store.record(historyEntry(id = "entry-2", scenario = PracticeScenario.meeting(), turnCount = 2))

        val dashboard = HomeDashboard.default(historyStore = store)

        assertEquals(3, dashboard.practiceStats.completedTurns)
        assertEquals(2, dashboard.practiceStats.todayCompletedSessions)
        assertEquals(1, dashboard.practiceStats.streakDays)
        assertEquals("meeting", dashboard.recentHistory?.scenarioId)
        assertEquals(2, dashboard.recentSummary?.turnCount)
    }

    private fun historyEntry(
        id: String,
        scenario: PracticeScenario,
        averageScore: Int = 82,
        turnCount: Int = 1,
        durationLabel: String = "03:25",
        completedAtEpochDay: Long = java.time.LocalDate.now().toEpochDay()
    ): PracticeHistoryEntry = PracticeHistoryEntry.fromSummary(
        id = id,
        scenario = scenario,
        summary = summary(averageScore = averageScore, turnCount = turnCount),
        completedAtLabel = "刚刚",
        durationLabel = durationLabel,
        completedAtEpochDay = completedAtEpochDay
    )

    private fun summary(
        averageScore: Int = 82,
        turnCount: Int = 1,
        nextGoal: String = "重复练习同一场景。"
    ): PracticeSummary = PracticeSummary(
        turnCount = turnCount,
        averageScore = averageScore,
        strengths = listOf("已完成练习流程。"),
        improvements = listOf("复习优化表达。"),
        nextGoal = nextGoal,
        scoreBreakdown = listOf(
            SummaryScoreBreakdown("语法", averageScore, "测试分数。"),
            SummaryScoreBreakdown("流利度", averageScore, "测试分数。"),
            SummaryScoreBreakdown("发音", averageScore, "测试分数。"),
            SummaryScoreBreakdown("完成度", averageScore, "测试分数。"),
            SummaryScoreBreakdown("词汇", averageScore, "测试分数。")
        )
    )

    private class FakePracticeHistoryStorage(initialValue: String? = null) : PracticeHistoryStorage {
        var savedValue: String? = initialValue
            private set

        override fun read(): String? = savedValue

        override fun write(value: String) {
            savedValue = value
        }

        override fun clear() {
            savedValue = null
        }
    }
}
