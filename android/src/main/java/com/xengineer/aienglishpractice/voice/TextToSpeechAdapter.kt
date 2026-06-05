package com.xengineer.aienglishpractice.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechAdapter(
    context: Context,
    private val onReadyChanged: (Boolean) -> Unit = {}
) : TextToSpeech.OnInitListener {
    private var ready = false
    private val tts = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            val result = tts.setLanguage(Locale.US)
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
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
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
        ready = false
        onReadyChanged(false)
    }
}
