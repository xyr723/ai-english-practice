package com.xengineer.aienglishpractice.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.xengineer.aienglishpractice.core.RecognitionAlternative
import com.xengineer.aienglishpractice.core.SpeechListenMode
import com.xengineer.aienglishpractice.core.SpeechTranscriptFormatter
import java.util.Locale

class SpeechRecognizerAdapter(
    private val context: Context,
    private val languageTag: String = Locale.US.toLanguageTag()
) {
    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(
        callbacks: SpeechRecognitionCallbacks,
        listenMode: SpeechListenMode = SpeechListenMode.Extended
    ) {
        if (!isAvailable()) {
            callbacks.onError("当前设备不可用语音识别。")
            return
        }

        recognizer?.destroy()

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = speechRecognizer
        speechRecognizer.setRecognitionListener(AndroidRecognitionListener(callbacks))
        speechRecognizer.startListening(recognizerIntent(listenMode))
    }

    fun stopListening() {
        recognizer?.stopListening()
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun recognizerIntent(listenMode: SpeechListenMode): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                listenMode.completeSilenceMillis
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                listenMode.possiblyCompleteSilenceMillis
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                listenMode.minimumLengthMillis
            )
        }

    private class AndroidRecognitionListener(
        private val callbacks: SpeechRecognitionCallbacks
    ) : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            callbacks.onReady()
        }

        override fun onBeginningOfSpeech() {
            callbacks.onSpeechStarted()
        }

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            callbacks.onSpeechEnded()
        }

        override fun onError(error: Int) {
            callbacks.onError(error.toSpeechErrorMessage())
        }

        override fun onResults(results: Bundle?) {
            callbacks.onFinal(results.bestRecognitionResult())
        }

        override fun onPartialResults(partialResults: Bundle?) {
            callbacks.onPartial(SpeechTranscriptFormatter.normalize(partialResults.bestRecognitionText()))
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}

private val SpeechListenMode.completeSilenceMillis: Int
    get() = when (this) {
        SpeechListenMode.Standard -> 2500
        SpeechListenMode.Extended -> 9000
    }

private val SpeechListenMode.possiblyCompleteSilenceMillis: Int
    get() = when (this) {
        SpeechListenMode.Standard -> 1500
        SpeechListenMode.Extended -> 7000
    }

private val SpeechListenMode.minimumLengthMillis: Int
    get() = when (this) {
        SpeechListenMode.Standard -> 1200
        SpeechListenMode.Extended -> 6000
    }

data class SpeechRecognitionCallbacks(
    val onReady: () -> Unit,
    val onSpeechStarted: () -> Unit = {},
    val onSpeechEnded: () -> Unit = {},
    val onPartial: (String) -> Unit,
    val onFinal: (SpeechRecognitionResult) -> Unit,
    val onError: (String) -> Unit
)

data class SpeechRecognitionResult(
    val transcript: String,
    val confidence: Float?,
    val alternatives: List<RecognitionAlternative> = emptyList()
)

private fun Bundle?.bestRecognitionText(): String {
    val matches = this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
    return matches?.firstOrNull().orEmpty()
}

private fun Bundle?.bestRecognitionResult(): SpeechRecognitionResult {
    val matches = this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
    val confidenceScores = this?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
    val alternatives = matches.mapIndexed { index, match ->
        RecognitionAlternative(
            transcript = SpeechTranscriptFormatter.normalize(match),
            confidence = confidenceScores
                ?.getOrNull(index)
                ?.takeIf { it >= 0f }
                ?.coerceIn(0f, 1f)
        )
    }
    val confidence = alternatives.firstOrNull()?.confidence
    return SpeechRecognitionResult(
        transcript = SpeechTranscriptFormatter.normalize(bestRecognitionText()),
        confidence = confidence,
        alternatives = alternatives
    )
}

private fun Int.toSpeechErrorMessage(): String = when (this) {
    SpeechRecognizer.ERROR_AUDIO -> "录音失败。"
    SpeechRecognizer.ERROR_CLIENT -> "语音识别客户端异常。"
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "麦克风权限不可用。"
    SpeechRecognizer.ERROR_NETWORK -> "语音识别网络异常。"
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "语音识别网络超时。"
    SpeechRecognizer.ERROR_NO_MATCH -> "未识别到有效语音。"
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "语音识别器正忙。"
    SpeechRecognizer.ERROR_SERVER -> "语音识别服务异常。"
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "未检测到语音。"
    else -> "语音识别失败。"
}
