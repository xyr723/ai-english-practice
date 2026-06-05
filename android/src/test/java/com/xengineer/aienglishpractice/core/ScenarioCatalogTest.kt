package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioCatalogTest {
    @Test
    fun catalogContainsRestaurantInterviewAndMeetingScenarios() {
        val scenarios = ScenarioCatalog.all()

        assertEquals(listOf("restaurant", "interview", "meeting"), scenarios.map { it.id })
        assertTrue(scenarios.all { it.name.isNotBlank() })
        assertTrue(scenarios.all { it.description.isNotBlank() })
        assertTrue(scenarios.all { it.estimatedMinutes in 6..12 })
    }

    @Test
    fun findByIdReturnsScenarioMetadataForDetailPage() {
        val scenario = ScenarioCatalog.findById("interview")

        assertNotNull(scenario)
        requireNotNull(scenario)
        assertEquals("面试练习", scenario.name)
        assertEquals("面试官", scenario.role)
        assertEquals("A2-B1", scenario.level)
        assertTrue(scenario.sceneTone.contains("正式"))
        assertTrue(scenario.goals.contains("describe_experience"))
    }

    @Test
    fun practiceSessionUsesSelectedScenarioTurns() {
        val scenario = ScenarioCatalog.findById("meeting")
        requireNotNull(scenario)
        val session = PracticeSession(
            scenario = scenario,
            correctionEngine = RuleCorrectionEngine(),
            scoreEngine = ScoreEngine()
        )

        session.start()
        val result = session.submitTurn(
            text = "I think we should discuss the timeline, please",
            durationMs = 7000,
            asrConfidence = 0.86f
        )

        assertEquals("Thanks. What is the main timeline risk?", result.reply)
    }

    @Test
    fun selectedScenarioKeywordsContributeToCompletionScore() {
        val scenario = ScenarioCatalog.findById("meeting")
        requireNotNull(scenario)
        val session = PracticeSession(
            scenario = scenario,
            correctionEngine = RuleCorrectionEngine(),
            scoreEngine = ScoreEngine()
        )

        session.start()
        val result = session.submitTurn(
            text = "I think the timeline risk is high, and the next step should be a shorter plan.",
            durationMs = 9000,
            asrConfidence = 0.9f
        )

        assertTrue(result.scores.completion.score > 0)
    }
}
