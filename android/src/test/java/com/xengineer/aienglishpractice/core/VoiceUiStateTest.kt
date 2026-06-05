package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceUiStateTest {
    @Test
    fun startsInDemoFallbackWhenSpeechPermissionIsMissing() {
        val state = VoiceUiState.initial(
            recognizerAvailable = true,
            audioPermissionGranted = false
        )

        assertEquals(VoiceInputMode.DemoFallback, state.mode)
        assertFalse(state.canStartSpeech)
        assertTrue(state.statusText.contains("Demo"))
        assertEquals("Use Speech", state.modeAction)
    }

    @Test
    fun speechModeCanStartWhenRecognizerAndPermissionAreReady() {
        val state = VoiceUiState.initial(
            recognizerAvailable = true,
            audioPermissionGranted = true
        ).useSpeechMode()

        assertEquals(VoiceInputMode.SpeechRecognizer, state.mode)
        assertTrue(state.canStartSpeech)
        assertEquals("Start Speech", state.speechAction)
    }

    @Test
    fun listeningStateKeepsPartialAndFinalTranscripts() {
        val listening = VoiceUiState.initial(
            recognizerAvailable = true,
            audioPermissionGranted = true
        ).useSpeechMode().startListening()

        val partial = listening.withPartialTranscript("I'd like")
        val final = partial.withFinalTranscript("I'd like a coffee, please.")

        assertTrue(listening.isListening)
        assertEquals("I'd like", partial.partialTranscript)
        assertEquals("I'd like a coffee, please.", final.finalTranscript)
        assertFalse(final.isListening)
        assertEquals("I'd like a coffee, please.", final.bestTranscript)
    }

    @Test
    fun recognitionErrorFallsBackToRecoverableDemoMode() {
        val state = VoiceUiState.initial(
            recognizerAvailable = true,
            audioPermissionGranted = true
        ).useSpeechMode().startListening().withRecognitionError("No speech detected.")

        assertEquals(VoiceInputMode.DemoFallback, state.mode)
        assertFalse(state.isListening)
        assertTrue(state.errorMessage.orEmpty().contains("No speech"))
        assertEquals("Use Speech", state.modeAction)
    }

    @Test
    fun ttsCanBeDisabledWithoutChangingSpeechMode() {
        val state = VoiceUiState.initial(
            recognizerAvailable = true,
            audioPermissionGranted = true
        ).useSpeechMode().setTtsEnabled(false)

        assertEquals(VoiceInputMode.SpeechRecognizer, state.mode)
        assertFalse(state.ttsEnabled)
        assertEquals("Enable TTS", state.ttsAction)
    }
}
