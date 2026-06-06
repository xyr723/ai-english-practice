package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineSelectionConfigTest {
    @Test
    fun defaultProfileMatchesCurrentSystemAndLocalEngines() {
        val config = EngineSelectionConfig.default()

        assertEquals(EngineProfileMode.StableDemo, config.profile)
        assertEquals(AsrEngineMode.AndroidSpeechRecognizer, config.asrEngine)
        assertEquals(TtsEngineMode.AndroidTextToSpeech, config.ttsEngine)
        assertEquals(EvaluationEngineMode.LocalRulesLanguageTool, config.evaluationEngine)
        assertTrue(config.engineSummaries.any { it.contains("Android SpeechRecognizer") })
        assertTrue(config.engineSummaries.any { it.contains("Android TextToSpeech") })
        assertTrue(config.engineSummaries.any { it.contains("LanguageTool") })
    }

    @Test
    fun accuracyProfileReservesCloudEnhancedEngines() {
        val config = EngineSelectionConfig.default().useAccuracyFirst()

        assertEquals(EngineProfileMode.AccuracyFirst, config.profile)
        assertEquals(AsrEngineMode.CloudAsr, config.asrEngine)
        assertEquals(TtsEngineMode.CloudNeuralTts, config.ttsEngine)
        assertEquals(EvaluationEngineMode.CloudCoachHybrid, config.evaluationEngine)
        assertTrue(config.engineSummaries.all { it.contains("预留") })
    }

    @Test
    fun accuracyProfileKeepsRuntimeFallbackUntilCloudEnginesAreConnected() {
        val config = EngineSelectionConfig.default().useAccuracyFirst()

        assertEquals(CoachBackendMode.Auto, config.preferredBackendMode)
        assertTrue(config.runtimeSummaries.any { it.contains("回退 Android SpeechRecognizer") })
        assertTrue(config.runtimeSummaries.any { it.contains("回退 Android TextToSpeech") })
        assertTrue(config.runtimeSummaries.any { it.contains("云端优先，本地兜底") })
    }

    @Test
    fun offlineProfileKeepsDeviceSideEngines() {
        val config = EngineSelectionConfig.default().useOfflineFirst()

        assertEquals(EngineProfileMode.OfflineFirst, config.profile)
        assertEquals(AsrEngineMode.AndroidSpeechRecognizer, config.asrEngine)
        assertEquals(TtsEngineMode.AndroidTextToSpeech, config.ttsEngine)
        assertEquals(EvaluationEngineMode.LocalRules, config.evaluationEngine)
        assertEquals(CoachBackendMode.LocalOnly, config.preferredBackendMode)
    }
}
