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
            PracticeState.Idle -> "准备开始"
            PracticeState.Listening -> "正在聆听"
            PracticeState.Recognizing -> "识别完成"
            PracticeState.Thinking -> "教练分析中"
            PracticeState.Speaking -> "反馈已生成"
            PracticeState.Finished -> "练习完成"
            PracticeState.Error -> "需要处理"
        }

    val statusBody: String
        get() = when (phase) {
            PracticeState.Idle -> "教练开场：${scenario.opening}"
            PracticeState.Listening -> "请用英语完成一段回答，场景：${scenario.name}。"
            PracticeState.Recognizing -> "先检查识别文本，再提交给教练分析。"
            PracticeState.Thinking -> "正在检查语法、表达、发音置信度和目标完成度。"
            PracticeState.Speaking -> "查看优化表达、评分细节和教练回复。"
            PracticeState.Finished -> "查看本次总结，准备好后可重新练习。"
            PracticeState.Error -> errorMessage ?: "恢复后继续当前场景。"
        }

    val primaryAction: String
        get() = when (phase) {
            PracticeState.Idle -> "开始练习"
            PracticeState.Listening -> "识别演示"
            PracticeState.Recognizing -> "提交给教练"
            PracticeState.Thinking -> "查看反馈"
            PracticeState.Speaking -> "下一轮"
            PracticeState.Finished -> "重新开始"
            PracticeState.Error -> "恢复练习"
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

        fun listening(
            scenario: PracticeScenario,
            turnResult: TurnResult? = null
        ): PracticeUiState = forPhase(
            scenario = scenario,
            phase = PracticeState.Listening,
            turnResult = turnResult
        )

        fun recognizing(
            scenario: PracticeScenario,
            transcript: String,
            turnResult: TurnResult? = null
        ): PracticeUiState = forPhase(
            scenario = scenario,
            phase = PracticeState.Recognizing,
            transcript = transcript,
            turnResult = turnResult
        )

        fun thinking(
            scenario: PracticeScenario,
            transcript: String,
            turnResult: TurnResult? = null
        ): PracticeUiState = forPhase(
            scenario = scenario,
            phase = PracticeState.Thinking,
            transcript = transcript,
            turnResult = turnResult
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
    PracticeState.Idle -> "准备"
    PracticeState.Listening -> "聆听"
    PracticeState.Recognizing -> "识别"
    PracticeState.Thinking -> "分析"
    PracticeState.Speaking -> "反馈"
    PracticeState.Finished -> "总结"
    PracticeState.Error -> "恢复"
}
