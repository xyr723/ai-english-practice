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
        assertTrue(state.statusText.contains("演示"))
        assertEquals("语音模式", state.modeAction)
    }

    @Test
    fun speechModeCanStartWhenRecognizerAndPermissionAreReady() {
        val state = VoiceUiState.initial(
            recognizerAvailable = true,
            audioPermissionGranted = true
        ).useSpeechMode()

        assertEquals(VoiceInputMode.SpeechRecognizer, state.mode)
        assertTrue(state.canStartSpeech)
        assertEquals("开始语音", state.speechAction)
    }

    @Test
    fun extendedListeningHasDistinctStateAndCopy() {
        val state = VoiceUiState.initial(
            recognizerAvailable = true,
            audioPermissionGranted = true
        ).useSpeechMode().startListening(SpeechListenMode.Extended)

        assertEquals(SpeechListenMode.Extended, state.listenMode)
        assertTrue(state.statusText.contains("长时"))
        assertEquals("长时聆听中...", state.speechAction)
    }

    @Test
    fun speechStartCanRecoverFromDemoModeWhenCapabilitiesAreReady() {
        val state = VoiceUiState.initial(
            recognizerAvailable = true,
            audioPermissionGranted = true
        ).useDemoMode().startSpeechFromCurrentMode()

        assertEquals(VoiceInputMode.SpeechRecognizer, state.mode)
        assertTrue(state.isListening)
        assertEquals(null, state.errorMessage)
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
        assertEquals("语音模式", state.modeAction)
    }

    @Test
    fun ttsCanBeDisabledWithoutChangingSpeechMode() {
        val state = VoiceUiState.initial(
            recognizerAvailable = true,
            audioPermissionGranted = true
        ).useSpeechMode().setTtsEnabled(false)

        assertEquals(VoiceInputMode.SpeechRecognizer, state.mode)
        assertFalse(state.ttsEnabled)
        assertEquals("打开朗读", state.ttsAction)
    }

    @Test
    fun ttsPlaybackStateCanDriveCharacterSpeakingAnimation() {
        val state = VoiceUiState.initial(
            recognizerAvailable = true,
            audioPermissionGranted = true
        )

        val speaking = state.setTtsSpeaking(true)
        val idle = speaking.setTtsSpeaking(false)

        assertTrue(speaking.isTtsSpeaking)
        assertFalse(idle.isTtsSpeaking)
    }
}
