package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeUiStateTest {
    @Test
    fun initialStateShowsScenarioOpeningAndStartAction() {
        val scenario = PracticeScenario.restaurant()
        val state = PracticeUiState.initial(scenario)

        assertEquals(PracticeState.Idle, state.phase)
        assertEquals("准备开始", state.statusTitle)
        assertTrue(state.statusBody.contains(scenario.opening))
        assertEquals("开始练习", state.primaryAction)
        assertEquals(0, state.stepIndex)
    }

    @Test
    fun everyPracticeStateHasUserFacingCopyAndTimelineMarker() {
        PracticeState.entries.forEach { phase ->
            val state = PracticeUiState.forPhase(
                scenario = PracticeScenario.restaurant(),
                phase = phase,
                transcript = "I'd like a coffee, please.",
                errorMessage = "Microphone permission is unavailable."
            )

            assertTrue(state.statusTitle.isNotBlank())
            assertTrue(state.statusBody.isNotBlank())
            assertTrue(state.primaryAction.isNotBlank())
            assertTrue(state.timeline.any { step -> step.phase == phase && step.isActive })
        }
    }

    @Test
    fun recognizingStateKeepsTranscriptBeforeFeedback() {
        val transcript = "I'd like a coffee, please."
        val state = PracticeUiState.recognizing(
            scenario = PracticeScenario.restaurant(),
            transcript = transcript
        )

        assertEquals(PracticeState.Recognizing, state.phase)
        assertEquals(transcript, state.transcript)
        assertEquals("提交给教练", state.primaryAction)
        assertFalse(state.canFinish)
    }

    @Test
    fun listeningAndRecognizingCanKeepPreviousCoachReply() {
        val previousResult = restaurantTurnResult(
            reply = "No problem. Your order will be ready soon."
        )

        val listening = PracticeUiState.listening(
            scenario = PracticeScenario.restaurant(),
            turnResult = previousResult
        )
        val recognizing = PracticeUiState.recognizing(
            scenario = PracticeScenario.restaurant(),
            transcript = "Thank you",
            turnResult = previousResult
        )

        assertEquals(previousResult, listening.turnResult)
        assertEquals(previousResult, recognizing.turnResult)
    }

    @Test
    fun speakingStateAllowsNextTurnAndFinish() {
        val result = restaurantTurnResult(
            reply = "Sure. Would you like anything to drink?"
        )
        val state = PracticeUiState.speaking(
            scenario = PracticeScenario.restaurant(),
            turnResult = result
        )

        assertEquals(PracticeState.Speaking, state.phase)
        assertEquals("下一轮", state.primaryAction)
        assertTrue(state.canFinish)
        assertEquals(4, state.stepIndex)
    }

    @Test
    fun finishedStateShowsSummaryAndRestartAction() {
        val summary = PracticeSummary(
            turnCount = 1,
            averageScore = 82,
            strengths = listOf("You completed the practice flow."),
            improvements = listOf("Review corrected expressions."),
            nextGoal = "Practice takeaway options."
        )
        val state = PracticeUiState.finished(
            scenario = PracticeScenario.restaurant(),
            summary = summary
        )

        assertEquals(PracticeState.Finished, state.phase)
        assertEquals("练习完成", state.statusTitle)
        assertEquals("重新开始", state.primaryAction)
        assertEquals(summary, state.summary)
        assertEquals(5, state.stepIndex)
    }

    @Test
    fun errorStateKeepsRecoveryAction() {
        val state = PracticeUiState.error(
            scenario = PracticeScenario.restaurant(),
            message = "Microphone permission is unavailable."
        )

        assertEquals(PracticeState.Error, state.phase)
        assertEquals("恢复练习", state.primaryAction)
        assertTrue(state.canRetry)
        assertTrue(state.statusBody.contains("Microphone permission"))
    }

    private fun restaurantTurnResult(reply: String): TurnResult = TurnResult(
        userText = "I want order a coffee",
        betterExpression = "I'd like to order a coffee, please.",
        reply = reply,
        scores = ScoreBundle(
            grammar = ScoreDetail(84, "Found 2 issues."),
            fluency = ScoreDetail(100, "Clear pace."),
            pronunciation = ScoreDetail(80, "ASR confidence is 0.80."),
            completion = ScoreDetail(66, "Matched 2 of 3 goals.")
        ),
        tips = listOf("Add a polite expression.")
    )
}
