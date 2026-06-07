package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechTranscriptFormatterTest {
    @Test
    fun normalizeSpeechTextConvertsAllCapsRecognitionToSentenceCase() {
        val normalized = SpeechTranscriptFormatter.normalize("I WANT ORDER A COFFEE PLEASE")

        assertEquals("I want order a coffee please", normalized)
    }

    @Test
    fun normalizeSpeechTextKeepsMixedCaseTextStable() {
        val normalized = SpeechTranscriptFormatter.normalize("I'd like to order a coffee, please.")

        assertEquals("I'd like to order a coffee, please.", normalized)
    }
}
