package com.xengineer.aienglishpractice.core

enum class EngineProfileMode(
    val title: String,
    val description: String
) {
    StableDemo(
        title = "稳定演示",
        description = "沿用当前设备侧 ASR/TTS 与本地规则 + LanguageTool 判定链路。"
    ),
    AccuracyFirst(
        title = "效果优先",
        description = "预留云端 ASR、神经 TTS 与云端教练混合判定入口。"
    ),
    OfflineFirst(
        title = "离线优先",
        description = "优先设备侧能力和本地规则，弱网环境下保持可用。"
    )
}

enum class EngineAvailability(val label: String) {
    Active("当前"),
    Reserved("预留")
}

enum class AsrEngineMode(
    val title: String,
    val description: String,
    val availability: EngineAvailability
) {
    AndroidSpeechRecognizer(
        title = "Android SpeechRecognizer",
        description = "系统自带语音识别，适合稳定演示和低接入成本。",
        availability = EngineAvailability.Active
    ),
    CloudAsr(
        title = "云端 ASR / PaddleSpeech",
        description = "为更长语句、更高准确率和可扩展模型预留。",
        availability = EngineAvailability.Reserved
    )
}

enum class TtsEngineMode(
    val title: String,
    val description: String,
    val availability: EngineAvailability
) {
    AndroidTextToSpeech(
        title = "Android TextToSpeech",
        description = "系统自带朗读，离线和演示链路可直接使用。",
        availability = EngineAvailability.Active
    ),
    CloudNeuralTts(
        title = "云端神经 TTS",
        description = "为更自然音色、角色音色和多语言语速控制预留。",
        availability = EngineAvailability.Reserved
    )
}

enum class EvaluationEngineMode(
    val title: String,
    val description: String,
    val availability: EngineAvailability
) {
    LocalRulesLanguageTool(
        title = "本地规则 + LanguageTool",
        description = "当前主链路，覆盖语法、礼貌表达和基础评分。",
        availability = EngineAvailability.Active
    ),
    LocalRules(
        title = "本地规则",
        description = "离线兜底判定，保留核心场景目标和基础纠错。",
        availability = EngineAvailability.Active
    ),
    CloudCoachHybrid(
        title = "云端教练混合判定",
        description = "为 LLM、LanguageTool 和规则库协同评分预留。",
        availability = EngineAvailability.Reserved
    )
}

data class EngineSelectionConfig(
    val profile: EngineProfileMode,
    val asrEngine: AsrEngineMode,
    val ttsEngine: TtsEngineMode,
    val evaluationEngine: EvaluationEngineMode
) {
    val preferredBackendMode: CoachBackendMode
        get() = when (evaluationEngine) {
            EvaluationEngineMode.LocalRules -> CoachBackendMode.LocalOnly
            EvaluationEngineMode.LocalRulesLanguageTool,
            EvaluationEngineMode.CloudCoachHybrid -> CoachBackendMode.Auto
        }

    val engineSummaries: List<String>
        get() = listOf(
            asrEngine.summary("ASR"),
            ttsEngine.summary("TTS"),
            evaluationEngine.summary("判定")
        )

    val runtimeSummaries: List<String>
        get() = listOf(
            asrEngine.runtimeSummary(),
            ttsEngine.runtimeSummary(),
            evaluationEngine.runtimeSummary()
        )

    fun useStableDemo(): EngineSelectionConfig = copy(
        profile = EngineProfileMode.StableDemo,
        asrEngine = AsrEngineMode.AndroidSpeechRecognizer,
        ttsEngine = TtsEngineMode.AndroidTextToSpeech,
        evaluationEngine = EvaluationEngineMode.LocalRulesLanguageTool
    )

    fun useAccuracyFirst(): EngineSelectionConfig = copy(
        profile = EngineProfileMode.AccuracyFirst,
        asrEngine = AsrEngineMode.CloudAsr,
        ttsEngine = TtsEngineMode.CloudNeuralTts,
        evaluationEngine = EvaluationEngineMode.CloudCoachHybrid
    )

    fun useOfflineFirst(): EngineSelectionConfig = copy(
        profile = EngineProfileMode.OfflineFirst,
        asrEngine = AsrEngineMode.AndroidSpeechRecognizer,
        ttsEngine = TtsEngineMode.AndroidTextToSpeech,
        evaluationEngine = EvaluationEngineMode.LocalRules
    )

    companion object {
        fun default(): EngineSelectionConfig = EngineSelectionConfig(
            profile = EngineProfileMode.StableDemo,
            asrEngine = AsrEngineMode.AndroidSpeechRecognizer,
            ttsEngine = TtsEngineMode.AndroidTextToSpeech,
            evaluationEngine = EvaluationEngineMode.LocalRulesLanguageTool
        )
    }
}

private fun AsrEngineMode.summary(role: String): String = "$role：$title（${availability.label}）"

private fun TtsEngineMode.summary(role: String): String = "$role：$title（${availability.label}）"

private fun EvaluationEngineMode.summary(role: String): String = "$role：$title（${availability.label}）"

private fun AsrEngineMode.runtimeSummary(): String = when (this) {
    AsrEngineMode.AndroidSpeechRecognizer -> "ASR：Android SpeechRecognizer（当前生效）"
    AsrEngineMode.CloudAsr -> "ASR：云端 ASR / PaddleSpeech（预留，当前回退 Android SpeechRecognizer）"
}

private fun TtsEngineMode.runtimeSummary(): String = when (this) {
    TtsEngineMode.AndroidTextToSpeech -> "TTS：Android TextToSpeech（当前生效）"
    TtsEngineMode.CloudNeuralTts -> "TTS：云端神经 TTS（预留，当前回退 Android TextToSpeech）"
}

private fun EvaluationEngineMode.runtimeSummary(): String = when (this) {
    EvaluationEngineMode.LocalRulesLanguageTool -> "判定：本地规则 + LanguageTool（云端优先，本地兜底）"
    EvaluationEngineMode.LocalRules -> "判定：本地规则（仅本地分析）"
    EvaluationEngineMode.CloudCoachHybrid -> "判定：云端教练混合判定（预留，当前云端优先，本地兜底）"
}
