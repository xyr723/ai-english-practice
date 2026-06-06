package com.xengineer.aienglishpractice.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TextToSpeechAdapter(
    context: Context,
    private val onReadyChanged: (Boolean) -> Unit = {},
    private val onPlaybackChanged: (Boolean) -> Unit = {}
) : TextToSpeech.OnInitListener {
    private var ready = false
    private val tts = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            val result = tts.setLanguage(Locale.US)
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            tts.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        onPlaybackChanged(true)
                    }

                    override fun onDone(utteranceId: String?) {
                        onPlaybackChanged(false)
                    }

                    override fun onError(utteranceId: String?) {
                        onPlaybackChanged(false)
                    }
                }
            )
        }
        onReadyChanged(ready)
    }

    fun isReady(): Boolean = ready

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "coach-reply")
    }

    fun stop() {
        tts.stop()
        onPlaybackChanged(false)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
        ready = false
        onReadyChanged(false)
        onPlaybackChanged(false)
    }
}
