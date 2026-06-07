package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Test

class CoachCharacterStateTest {
    @Test
    fun ttsSpeakingTakesPriorityOverPracticePhase() {
        val state = CoachCharacterState.from(
            practiceState = PracticeState.Thinking,
            isTtsSpeaking = true
        )

        assertEquals(CoachCharacterState.Speaking, state)
    }

    @Test
    fun listeningAndRecognizingUseListeningAnimation() {
        assertEquals(
            CoachCharacterState.Listening,
            CoachCharacterState.from(PracticeState.Listening, isTtsSpeaking = false)
        )
        assertEquals(
            CoachCharacterState.Listening,
            CoachCharacterState.from(PracticeState.Recognizing, isTtsSpeaking = false)
        )
    }

    @Test
    fun thinkingUsesThinkingAnimationAndOtherStatesIdle() {
        assertEquals(
            CoachCharacterState.Thinking,
            CoachCharacterState.from(PracticeState.Thinking, isTtsSpeaking = false)
        )
        assertEquals(
            CoachCharacterState.Idle,
            CoachCharacterState.from(PracticeState.Speaking, isTtsSpeaking = false)
        )
    }
}
