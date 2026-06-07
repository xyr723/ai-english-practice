package com.xengineer.aienglishpractice.ui.history

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryScreenContractTest {
    @Test
    fun historyScreenUsesCompactTimelineRows() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/history/HistoryScreen.kt").readText()

        assertTrue(source.contains("HistoryTimelineList"))
        assertTrue(source.contains("HistoryTimelineRow"))
        assertTrue(source.contains("LazyColumn"))
        assertTrue(source.contains("itemsIndexed(entries)"))
        assertTrue(source.contains("全部场景"))
        assertTrue(source.contains("清空记录"))
    }

    @Test
    fun historyRowsOpenReviewInsteadOfStartingNewPracticeAndUseIcons() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/history/HistoryScreen.kt").readText()

        assertTrue(source.contains("onOpenReview"))
        assertTrue(source.contains("HistoryReviewScreen"))
        assertTrue(source.contains("scenarioIconFor"))
        assertTrue(source.contains("R.drawable.ic_play_circle"))
        assertTrue(source.contains("回顾"))
        assertTrue(source.contains("entry.toSummary()"))
        assertTrue(source.contains("entry.durationLabel"))
        assertTrue(source.contains("未记录"))
        assertTrue(!source.contains("entry.turnCount * 16"))
    }

    @Test
    fun historyReviewUsesSharedAbilityRadarChart() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/history/HistoryScreen.kt").readText()

        assertTrue(source.contains("AbilityRadarChart"))
        assertTrue(source.contains("summary.scoreBreakdown"))
        assertTrue(source.contains("能力雷达"))
    }
}
