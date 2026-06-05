package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeSessionTest {
    @Test
    fun restaurantPracticeCompletesOneTurnWithCorrectionScoreAndReply() {
        val session = PracticeSession(
            scenario = PracticeScenario.restaurant(),
            correctionEngine = RuleCorrectionEngine(),
            scoreEngine = ScoreEngine()
        )

        session.start()
        val result = session.submitTurn(
            text = "I want order a coffee",
            durationMs = 6000,
            asrConfidence = 0.8f
        )

        assertEquals(PracticeState.Speaking, session.state)
        assertEquals("I'd like to order a coffee, please.", result.betterExpression)
        assertEquals("Sure. Would you like anything to drink?", result.reply)
        assertEquals(84, result.scores.grammar.score)
        assertEquals(80, result.scores.pronunciation.score)
        assertTrue(result.tips.isNotEmpty())
    }

    @Test
    fun finishPracticeBuildsSummaryFromRecordedTurns() {
        val session = PracticeSession(
            scenario = PracticeScenario.restaurant(),
            correctionEngine = RuleCorrectionEngine(),
            scoreEngine = ScoreEngine()
        )

        session.start()
        session.submitTurn("I'd like to order a coffee, please.", durationMs = 6000, asrConfidence = 0.9f)
        val summary = session.finish()

        assertEquals(PracticeState.Finished, session.state)
        assertEquals(1, summary.turnCount)
        assertTrue(summary.averageScore > 0)
        assertTrue(summary.nextGoal.isNotBlank())
    }
}
