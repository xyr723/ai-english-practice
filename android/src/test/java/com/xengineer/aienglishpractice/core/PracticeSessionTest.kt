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
        assertEquals("当然。你想喝点什么吗？", result.replyTranslation)
        assertEquals(84, result.scores.grammar.score)
        assertEquals(80, result.scores.pronunciation.score)
        assertTrue(result.tips.isNotEmpty())
    }

    @Test
    fun restaurantReplySkipsDrinkQuestionWhenFoodAndDrinkAreAlreadyOrdered() {
        val session = PracticeSession(
            scenario = PracticeScenario.restaurant(),
            correctionEngine = RuleCorrectionEngine(),
            scoreEngine = ScoreEngine()
        )

        session.start()
        val result = session.submitTurn(
            text = "I want a cup of tea and a hamburger",
            durationMs = 6200,
            asrConfidence = 0.86f
        )

        assertEquals("Great. Is that for here or takeaway?", result.reply)
        assertEquals("好的。是在这里吃还是外带？", result.replyTranslation)
    }

    @Test
    fun finishPracticeBuildsSummaryFromRecordedTurns() {
        val session = PracticeSession(
            scenario = PracticeScenario.restaurant(),
            correctionEngine = RuleCorrectionEngine(),
            scoreEngine = ScoreEngine()
        )

        session.start()
        session.submitTurn("I want order a coffee", durationMs = 6000, asrConfidence = 0.8f)
        session.submitTurn("For here, please.", durationMs = 4500, asrConfidence = 0.9f)
        val summary = session.finish()

        assertEquals(PracticeState.Finished, session.state)
        assertEquals(2, summary.turnCount)
        assertTrue(summary.averageScore > 0)
        assertTrue(summary.nextGoal.isNotBlank())
        assertEquals(5, summary.scoreBreakdown.size)
        assertEquals(listOf("语法", "流利度", "发音", "完成度", "词汇"), summary.scoreBreakdown.map { it.label })
        assertEquals(2, summary.turnReviews.size)
        assertEquals("I want order a coffee", summary.turnReviews.first().userText)
        assertEquals("I'd like to order a coffee, please.", summary.turnReviews.first().betterExpression)
        assertTrue(summary.turnReviews.first().tips.isNotEmpty())
        assertTrue(summary.practicePlan.size >= 3)
    }

    @Test
    fun localCorrectionFixesCommonVerbAgreementWithoutOnlyAppendingPlease() {
        val result = RuleCorrectionEngine().check(
            text = "I has a question",
            scenario = PracticeScenario.meeting()
        )

        assertEquals("I have a question.", result.betterExpression)
        assertTrue(result.issues.any { it.type == "grammar" })
    }
}
