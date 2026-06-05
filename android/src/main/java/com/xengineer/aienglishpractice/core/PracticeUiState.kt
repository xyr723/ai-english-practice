package com.xengineer.aienglishpractice.core

data class PracticeUiState(
    val scenario: PracticeScenario,
    val phase: PracticeState,
    val transcript: String = "",
    val turnResult: TurnResult? = null,
    val summary: PracticeSummary? = null,
    val errorMessage: String? = null
) {
    val statusTitle: String
        get() = when (phase) {
            PracticeState.Idle -> "Ready"
            PracticeState.Listening -> "Listening"
            PracticeState.Recognizing -> "Recognized transcript"
            PracticeState.Thinking -> "Coach is checking"
            PracticeState.Speaking -> "Feedback ready"
            PracticeState.Finished -> "Session complete"
            PracticeState.Error -> "Something needs attention"
        }

    val statusBody: String
        get() = when (phase) {
            PracticeState.Idle -> "Coach says: ${scenario.opening}"
            PracticeState.Listening -> "Speak one complete answer for the ${scenario.name} scene."
            PracticeState.Recognizing -> "Review the transcript before sending it to the coach."
            PracticeState.Thinking -> "Checking grammar, expression, pronunciation confidence, and goal completion."
            PracticeState.Speaking -> "Review the improved expression, score details, and coach reply."
            PracticeState.Finished -> "Review the session summary and restart when ready."
            PracticeState.Error -> errorMessage ?: "Recover and try the current scene again."
        }

    val primaryAction: String
        get() = when (phase) {
            PracticeState.Idle -> "Start Listening"
            PracticeState.Listening -> "Recognize Demo"
            PracticeState.Recognizing -> "Ask Coach"
            PracticeState.Thinking -> "Show Feedback"
            PracticeState.Speaking -> "Next Turn"
            PracticeState.Finished -> "Restart Scene"
            PracticeState.Error -> "Recover"
        }

    val canFinish: Boolean
        get() = phase == PracticeState.Speaking

    val canRetry: Boolean
        get() = phase == PracticeState.Error

    val stepIndex: Int
        get() = when (phase) {
            PracticeState.Idle -> 0
            PracticeState.Listening -> 1
            PracticeState.Recognizing -> 2
            PracticeState.Thinking -> 3
            PracticeState.Speaking -> 4
            PracticeState.Finished -> 5
            PracticeState.Error -> 6
        }

    val timeline: List<PracticeStep>
        get() = PracticeState.entries.map { state ->
            PracticeStep(
                phase = state,
                label = state.stepLabel(),
                isActive = state == phase,
                isComplete = state.stepOrder() < phase.stepOrder()
            )
        }

    companion object {
        fun initial(scenario: PracticeScenario): PracticeUiState = PracticeUiState(
            scenario = scenario,
            phase = PracticeState.Idle
        )

        fun forPhase(
            scenario: PracticeScenario,
            phase: PracticeState,
            transcript: String = "",
            turnResult: TurnResult? = null,
            summary: PracticeSummary? = null,
            errorMessage: String? = null
        ): PracticeUiState = PracticeUiState(
            scenario = scenario,
            phase = phase,
            transcript = transcript,
            turnResult = turnResult,
            summary = summary,
            errorMessage = errorMessage
        )

        fun listening(scenario: PracticeScenario): PracticeUiState = forPhase(
            scenario = scenario,
            phase = PracticeState.Listening
        )

        fun recognizing(
            scenario: PracticeScenario,
            transcript: String
        ): PracticeUiState = forPhase(
            scenario = scenario,
            phase = PracticeState.Recognizing,
            transcript = transcript
        )

        fun thinking(
            scenario: PracticeScenario,
            transcript: String
        ): PracticeUiState = forPhase(
            scenario = scenario,
            phase = PracticeState.Thinking,
            transcript = transcript
        )

        fun speaking(
            scenario: PracticeScenario,
            turnResult: TurnResult
        ): PracticeUiState = forPhase(
            scenario = scenario,
            phase = PracticeState.Speaking,
            transcript = turnResult.userText,
            turnResult = turnResult
        )

        fun finished(
            scenario: PracticeScenario,
            summary: PracticeSummary?
        ): PracticeUiState = forPhase(
            scenario = scenario,
            phase = PracticeState.Finished,
            summary = summary
        )

        fun error(
            scenario: PracticeScenario,
            message: String
        ): PracticeUiState = forPhase(
            scenario = scenario,
            phase = PracticeState.Error,
            errorMessage = message
        )
    }
}

data class PracticeStep(
    val phase: PracticeState,
    val label: String,
    val isActive: Boolean,
    val isComplete: Boolean
)

private fun PracticeState.stepOrder(): Int = when (this) {
    PracticeState.Idle -> 0
    PracticeState.Listening -> 1
    PracticeState.Recognizing -> 2
    PracticeState.Thinking -> 3
    PracticeState.Speaking -> 4
    PracticeState.Finished -> 5
    PracticeState.Error -> 6
}

private fun PracticeState.stepLabel(): String = when (this) {
    PracticeState.Idle -> "Ready"
    PracticeState.Listening -> "Listen"
    PracticeState.Recognizing -> "ASR"
    PracticeState.Thinking -> "Check"
    PracticeState.Speaking -> "Feedback"
    PracticeState.Finished -> "Summary"
    PracticeState.Error -> "Recover"
}
