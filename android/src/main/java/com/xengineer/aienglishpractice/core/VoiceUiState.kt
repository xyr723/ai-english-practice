package com.xengineer.aienglishpractice.core

enum class VoiceInputMode {
    DemoFallback,
    SpeechRecognizer
}

enum class SpeechListenMode {
    Standard,
    Extended
}

data class VoiceUiState(
    val mode: VoiceInputMode,
    val recognizerAvailable: Boolean,
    val audioPermissionGranted: Boolean,
    val isListening: Boolean = false,
    val listenMode: SpeechListenMode = SpeechListenMode.Standard,
    val partialTranscript: String = "",
    val finalTranscript: String = "",
    val errorMessage: String? = null,
    val ttsEnabled: Boolean = true,
    val ttsReady: Boolean = false,
    val isTtsSpeaking: Boolean = false
) {
    val canStartSpeech: Boolean
        get() = mode == VoiceInputMode.SpeechRecognizer &&
            recognizerAvailable &&
            audioPermissionGranted &&
            !isListening

    val bestTranscript: String
        get() = finalTranscript.ifBlank { partialTranscript }

    val statusText: String
        get() = when {
            errorMessage != null -> errorMessage
            mode == VoiceInputMode.DemoFallback && !recognizerAvailable -> "语音不可用，可用演示。"
            mode == VoiceInputMode.DemoFallback && !audioPermissionGranted -> "需麦克风权限，可用演示。"
            mode == VoiceInputMode.DemoFallback -> "演示模式可用。"
            isListening && listenMode == SpeechListenMode.Extended -> "长时聆听中。"
            isListening -> "正在聆听。"
            bestTranscript.isNotBlank() -> "转写已就绪。"
            else -> "语音模式已就绪。"
        }

    val modeAction: String
        get() = if (mode == VoiceInputMode.SpeechRecognizer) "演示模式" else "语音模式"

    val speechAction: String
        get() = when {
            isListening && listenMode == SpeechListenMode.Extended -> "长时聆听中..."
            isListening -> "聆听中..."
            else -> "开始语音"
        }

    val ttsAction: String
        get() = if (ttsEnabled) "关闭自动朗读" else "打开自动朗读"

    fun useDemoMode(): VoiceUiState = copy(
        mode = VoiceInputMode.DemoFallback,
        isListening = false,
        listenMode = SpeechListenMode.Standard,
        isTtsSpeaking = false,
        errorMessage = null
    )

    fun useSpeechMode(): VoiceUiState = if (recognizerAvailable && audioPermissionGranted) {
        copy(mode = VoiceInputMode.SpeechRecognizer, errorMessage = null)
    } else {
        copy(mode = VoiceInputMode.DemoFallback)
    }

    fun withCapabilities(
        recognizerAvailable: Boolean,
        audioPermissionGranted: Boolean
    ): VoiceUiState {
        val nextMode = if (mode == VoiceInputMode.SpeechRecognizer && recognizerAvailable && audioPermissionGranted) {
            VoiceInputMode.SpeechRecognizer
        } else {
            VoiceInputMode.DemoFallback
        }

        return copy(
            mode = nextMode,
            recognizerAvailable = recognizerAvailable,
            audioPermissionGranted = audioPermissionGranted,
            isListening = if (nextMode == VoiceInputMode.SpeechRecognizer) isListening else false
        )
    }

    fun startListening(listenMode: SpeechListenMode = SpeechListenMode.Standard): VoiceUiState = if (canStartSpeech) {
        copy(
            isListening = true,
            listenMode = listenMode,
            partialTranscript = "",
            finalTranscript = "",
            errorMessage = null
        )
    } else {
        copy(
            mode = VoiceInputMode.DemoFallback,
            isListening = false,
            listenMode = SpeechListenMode.Standard,
            errorMessage = "语音输入未就绪，可使用演示模式。"
        )
    }

    fun startSpeechFromCurrentMode(
        listenMode: SpeechListenMode = SpeechListenMode.Standard
    ): VoiceUiState = useSpeechMode().startListening(listenMode)

    fun withPartialTranscript(text: String): VoiceUiState = copy(
        partialTranscript = text,
        errorMessage = null
    )

    fun withFinalTranscript(text: String): VoiceUiState = copy(
        isListening = false,
        listenMode = SpeechListenMode.Standard,
        finalTranscript = text,
        partialTranscript = "",
        errorMessage = null
    )

    fun withRecognitionError(message: String): VoiceUiState = copy(
        mode = if (recognizerAvailable && audioPermissionGranted) {
            VoiceInputMode.SpeechRecognizer
        } else {
            VoiceInputMode.DemoFallback
        },
        isListening = false,
        listenMode = SpeechListenMode.Standard,
        isTtsSpeaking = false,
        errorMessage = message
    )

    fun setTtsEnabled(enabled: Boolean): VoiceUiState = copy(
        ttsEnabled = enabled,
        isTtsSpeaking = if (enabled) isTtsSpeaking else false
    )

    fun setTtsReady(ready: Boolean): VoiceUiState = copy(
        ttsReady = ready,
        isTtsSpeaking = if (ready) isTtsSpeaking else false
    )

    fun setTtsSpeaking(speaking: Boolean): VoiceUiState = copy(isTtsSpeaking = speaking)

    companion object {
        fun initial(
            recognizerAvailable: Boolean,
            audioPermissionGranted: Boolean
        ): VoiceUiState = VoiceUiState(
            mode = if (recognizerAvailable && audioPermissionGranted) {
                VoiceInputMode.SpeechRecognizer
            } else {
                VoiceInputMode.DemoFallback
            },
            recognizerAvailable = recognizerAvailable,
            audioPermissionGranted = audioPermissionGranted
        )
    }
}
