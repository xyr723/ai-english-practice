package com.xengineer.aienglishpractice.core

enum class VoiceInputMode {
    DemoFallback,
    SpeechRecognizer
}

data class VoiceUiState(
    val mode: VoiceInputMode,
    val recognizerAvailable: Boolean,
    val audioPermissionGranted: Boolean,
    val isListening: Boolean = false,
    val partialTranscript: String = "",
    val finalTranscript: String = "",
    val errorMessage: String? = null,
    val ttsEnabled: Boolean = true,
    val ttsReady: Boolean = false
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
            mode == VoiceInputMode.DemoFallback && !recognizerAvailable -> "Speech recognition is unavailable. Demo fallback is ready."
            mode == VoiceInputMode.DemoFallback && !audioPermissionGranted -> "Demo fallback is ready. Grant microphone permission to use speech."
            mode == VoiceInputMode.DemoFallback -> "Demo fallback is ready."
            isListening -> "Listening with Android SpeechRecognizer."
            bestTranscript.isNotBlank() -> "Speech transcript is ready."
            else -> "Speech mode is ready."
        }

    val modeAction: String
        get() = if (mode == VoiceInputMode.SpeechRecognizer) "Use Demo" else "Use Speech"

    val speechAction: String
        get() = if (isListening) "Listening..." else "Start Speech"

    val ttsAction: String
        get() = if (ttsEnabled) "Disable TTS" else "Enable TTS"

    fun useDemoMode(): VoiceUiState = copy(
        mode = VoiceInputMode.DemoFallback,
        isListening = false,
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

    fun startListening(): VoiceUiState = if (canStartSpeech) {
        copy(
            isListening = true,
            partialTranscript = "",
            finalTranscript = "",
            errorMessage = null
        )
    } else {
        copy(
            mode = VoiceInputMode.DemoFallback,
            isListening = false,
            errorMessage = "Speech input is not ready. Demo fallback is available."
        )
    }

    fun withPartialTranscript(text: String): VoiceUiState = copy(
        partialTranscript = text,
        errorMessage = null
    )

    fun withFinalTranscript(text: String): VoiceUiState = copy(
        isListening = false,
        finalTranscript = text,
        partialTranscript = "",
        errorMessage = null
    )

    fun withRecognitionError(message: String): VoiceUiState = copy(
        mode = VoiceInputMode.DemoFallback,
        isListening = false,
        errorMessage = message
    )

    fun setTtsEnabled(enabled: Boolean): VoiceUiState = copy(ttsEnabled = enabled)

    fun setTtsReady(ready: Boolean): VoiceUiState = copy(ttsReady = ready)

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
