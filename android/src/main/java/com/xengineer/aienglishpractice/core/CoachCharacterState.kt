package com.xengineer.aienglishpractice.core

enum class CoachCharacterState {
    Idle,
    Listening,
    Thinking,
    Speaking;

    companion object {
        fun from(
            practiceState: PracticeState,
            isTtsSpeaking: Boolean
        ): CoachCharacterState = when {
            isTtsSpeaking -> Speaking
            practiceState == PracticeState.Listening ||
                practiceState == PracticeState.Recognizing -> Listening
            practiceState == PracticeState.Thinking -> Thinking
            else -> Idle
        }
    }
}
