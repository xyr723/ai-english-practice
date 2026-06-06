package com.xengineer.aienglishpractice.voice

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TextToSpeechAdapterContractTest {
    @Test
    fun ttsPlaybackCallbacksUseUtteranceProgressListener() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/voice/TextToSpeechAdapter.kt").readText()

        assertTrue(source.contains("UtteranceProgressListener"))
        assertTrue(source.contains("onStart"))
        assertTrue(source.contains("onDone"))
        assertTrue(source.contains("onPlaybackChanged(true)"))
        assertTrue(source.contains("onPlaybackChanged(false)"))
    }
}
