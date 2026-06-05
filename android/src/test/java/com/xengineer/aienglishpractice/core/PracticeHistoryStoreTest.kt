package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
            completedAtLabel = "今天 10:00"
        )

        assertEquals("entry-interview", entry.id)
        assertEquals("interview", entry.scenarioId)
        assertEquals("面试练习", entry.scenarioName)
        assertEquals("今天 10:00", entry.completedAtLabel)
        assertEquals(91, entry.averageScore)
        assertEquals(summary, entry.toSummary())
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
    fun homeDashboardUsesLatestLocalHistory() {
        val store = PracticeHistoryStore()
        store.record(historyEntry(id = "entry-1", scenario = PracticeScenario.restaurant(), turnCount = 1))
        store.record(historyEntry(id = "entry-2", scenario = PracticeScenario.meeting(), turnCount = 2))

        val dashboard = HomeDashboard.default(historyStore = store)

        assertEquals(3, dashboard.practiceStats.completedTurns)
        assertEquals("meeting", dashboard.recentHistory?.scenarioId)
        assertEquals(2, dashboard.recentSummary?.turnCount)
    }

    private fun historyEntry(
        id: String,
        scenario: PracticeScenario,
        averageScore: Int = 82,
        turnCount: Int = 1
    ): PracticeHistoryEntry = PracticeHistoryEntry.fromSummary(
        id = id,
        scenario = scenario,
        summary = summary(averageScore = averageScore, turnCount = turnCount),
        completedAtLabel = "刚刚"
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
        nextGoal = nextGoal
    )
}
