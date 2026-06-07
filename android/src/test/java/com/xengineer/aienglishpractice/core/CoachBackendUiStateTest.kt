package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachBackendUiStateTest {
    @Test
    fun autoModeTriesBackendAndAllowsFallbackOnFailure() {
        val state = CoachBackendUiState.initial()

        assertEquals(CoachBackendMode.Auto, state.mode)
        assertTrue(state.shouldTryBackend)

        val failed = state.withBackendFailure("Connection refused")

        assertTrue(failed.shouldUseLocalFallback)
        assertEquals(CoachFeedbackSource.LocalFallback, failed.lastSource)
        assertTrue(failed.statusText.contains("本地分析"))
    }

    @Test
    fun localOnlyNeverTriesBackend() {
        val state = CoachBackendUiState.initial().useLocalOnly()

        assertEquals(CoachBackendMode.LocalOnly, state.mode)
        assertFalse(state.shouldTryBackend)
        assertTrue(state.shouldUseLocalFallback)
        assertEquals("自动模式", state.modeAction)
    }

    @Test
    fun backendOnlyReportsFailureWithoutSilentFallback() {
        val state = CoachBackendUiState.initial().useBackendOnly()

        val failed = state.withBackendFailure("HTTP 500")

        assertEquals(CoachBackendMode.BackendOnly, failed.mode)
        assertFalse(failed.shouldUseLocalFallback)
        assertEquals(CoachFeedbackSource.BackendError, failed.lastSource)
        assertTrue(failed.statusText.contains("HTTP 500"))
    }

    @Test
    fun backendSuccessClearsErrorAndMarksBackendSource() {
        val state = CoachBackendUiState.initial().withBackendFailure("Timeout")

        val succeeded = state.withBackendSuccess()

        assertEquals(CoachFeedbackSource.BackendApi, succeeded.lastSource)
        assertEquals(null, succeeded.lastError)
        assertFalse(succeeded.isChecking)
    }

    @Test
    fun backendSuccessCanMarkLanguageToolSource() {
        val state = CoachBackendUiState.initial().withChecking()

        val succeeded = state.withBackendSuccess(CoachFeedbackSource.LanguageTool)

        assertEquals(CoachFeedbackSource.LanguageTool, succeeded.lastSource)
        assertTrue(succeeded.statusText.contains("LanguageTool"))
        assertFalse(succeeded.shouldUseLocalFallback)
    }

    @Test
    fun modeCyclesAutoBackendLocal() {
        val auto = CoachBackendUiState.initial()
        val backend = auto.nextMode()
        val local = backend.nextMode()
        val backToAuto = local.nextMode()

        assertEquals(CoachBackendMode.BackendOnly, backend.mode)
        assertEquals(CoachBackendMode.LocalOnly, local.mode)
        assertEquals(CoachBackendMode.Auto, backToAuto.mode)
    }
}
