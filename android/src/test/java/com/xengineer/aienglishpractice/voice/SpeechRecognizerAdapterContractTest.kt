package com.xengineer.aienglishpractice.voice

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechRecognizerAdapterContractTest {
    @Test
    fun recognizerAllowsLearnerPausesForExtendedEnglishPractice() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/voice/SpeechRecognizerAdapter.kt").readText()

        assertTrue(source.contains("listenMode: SpeechListenMode = SpeechListenMode.Extended"))
        assertTrue(source.contains("SpeechListenMode.Standard -> 1200"))
        assertTrue(source.contains("SpeechListenMode.Extended -> 9000"))
        assertTrue(source.contains("SpeechListenMode.Extended -> 7000"))
        assertTrue(source.contains("SpeechListenMode.Extended -> 6000"))
        assertFalse(source.contains("SpeechListenMode.Extended -> 15000"))
    }
}
