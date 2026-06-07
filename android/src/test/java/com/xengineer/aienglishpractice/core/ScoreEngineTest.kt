package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreEngineTest {
    @Test
    fun pronunciationScoreUsesRecognitionStabilityBeyondConfidence() {
        val scores = ScoreEngine().score(
            text = "I want to order a coffee",
            durationMs = 6000,
            asrConfidence = 0.82f,
            correction = CorrectionResult(
                original = "I want to order a coffee",
                betterExpression = "I want to order a coffee.",
                issues = emptyList()
            ),
            matchedGoals = 2,
            totalGoals = 3,
            recognitionAlternatives = listOf(
                RecognitionAlternative("I want to order a coffee", 0.82f),
                RecognitionAlternative("I want to order a copy", 0.58f),
                RecognitionAlternative("I want order coffee", 0.45f)
            )
        )

        assertEquals(74, scores.pronunciation.score)
        assertTrue(scores.pronunciation.reason.contains("候选句稳定度"))
        assertTrue(scores.pronunciation.reason.contains("coffee"))
    }
}
