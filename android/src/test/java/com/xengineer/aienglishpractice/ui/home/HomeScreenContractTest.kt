package com.xengineer.aienglishpractice.ui.home

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenContractTest {
    @Test
    fun homeScreenUsesVisualScenarioSelectionLayout() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/home/HomeScreen.kt").readText()

        assertTrue(source.contains("ScenarioSelectionHero"))
        assertTrue(source.contains("ScenarioTileGrid"))
        assertTrue(source.contains("今天想练习什么场景"))
        assertTrue(source.contains("连续练习"))
    }

    @Test
    fun homeScreenKeepsVisibleCustomScenarioEntryAndVectorIcons() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/home/HomeScreen.kt").readText()

        assertTrue(source.contains("自定义"))
        assertTrue(source.contains("自定义场景"))
        assertTrue(source.contains("painterResource"))
        assertTrue(source.contains("R.drawable.ic_"))
        assertTrue(source.contains("HomeTopAction"))
        assertTrue(source.contains("R.drawable.ic_history"))
        assertTrue(source.contains("R.drawable.ic_settings"))
        assertFalse(source.contains("TextButton(onClick = onOpenHistory)"))
        assertFalse(source.contains("TextButton(onClick = onOpenSettings)"))
        assertFalse(source.contains("\"人\""))
        assertFalse(source.contains("\"食\""))
    }

    @Test
    fun todayGoalProgressUsesDashboardStatsInsteadOfHardcodedZero() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/home/HomeScreen.kt").readText()

        assertTrue(source.contains("todayGoalProgress"))
        assertTrue(source.contains("dashboard.practiceStats.todayCompletedSessions.coerceIn(0, 1)"))
        assertTrue(source.contains("已完成 \$todayGoalProgress/1"))
        assertFalse(source.contains("Text(\"0/1\""))
    }

    @Test
    fun homeStatsDoesNotShowMoreButtonWhenCustomEntryIsAlreadyVisible() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/home/HomeScreen.kt").readText()

        assertTrue(source.contains("自定义场景"))
        assertFalse(source.contains("Text(\"更多\""))
    }

    @Test
    fun scenarioTilesExposeStableUiTestTagsForEndToEndSmokeTests() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/home/HomeScreen.kt").readText()

        assertTrue(source.contains("testTag"))
        assertTrue(source.contains("scenario-tile-\${spec.scenarioId ?: spec.prompt ?: spec.title}"))
    }
}
