package com.xengineer.aienglishpractice.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SpeechRecognizerAdapter(
    private val context: Context,
    private val languageTag: String = Locale.US.toLanguageTag()
) {
    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(callbacks: SpeechRecognitionCallbacks) {
        if (!isAvailable()) {
            callbacks.onError("Speech recognition is unavailable on this device.")
            return
        }

        recognizer?.destroy()

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = speechRecognizer
        speechRecognizer.setRecognitionListener(AndroidRecognitionListener(callbacks))
        speechRecognizer.startListening(recognizerIntent())
    }

    fun stopListening() {
        recognizer?.stopListening()
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun recognizerIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    }

    private class AndroidRecognitionListener(
        private val callbacks: SpeechRecognitionCallbacks
    ) : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            callbacks.onReady()
        }

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() = Unit

        override fun onError(error: Int) {
            callbacks.onError(error.toSpeechErrorMessage())
        }

        override fun onResults(results: Bundle?) {
            callbacks.onFinal(results.bestRecognitionText())
        }

        override fun onPartialResults(partialResults: Bundle?) {
            callbacks.onPartial(partialResults.bestRecognitionText())
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}

data class SpeechRecognitionCallbacks(
    val onReady: () -> Unit,
    val onPartial: (String) -> Unit,
    val onFinal: (String) -> Unit,
    val onError: (String) -> Unit
)

private fun Bundle?.bestRecognitionText(): String {
    val matches = this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
    return matches?.firstOrNull().orEmpty()
}

private fun Int.toSpeechErrorMessage(): String = when (this) {
    SpeechRecognizer.ERROR_AUDIO -> "Audio recording failed."
    SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error."
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is unavailable."
    SpeechRecognizer.ERROR_NETWORK -> "Network error while recognizing speech."
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network timeout."
    SpeechRecognizer.ERROR_NO_MATCH -> "No speech was recognized."
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy."
    SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error."
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected."
    else -> "Speech recognition failed."
}
