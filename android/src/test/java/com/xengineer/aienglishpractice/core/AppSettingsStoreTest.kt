package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsStoreTest {
    @Test
    fun persistsCustomCloudflareEndpointAcrossStoreInstances() {
        val storage = FakeAppSettingsStorage()
        val cloudflareConfig = CoachEndpointConfig.default()
            .useCustom("  https://demo.trycloudflare.com/  ")

        AppSettingsStore(storage).saveEndpointConfig(cloudflareConfig)
        val restored = AppSettingsStore(storage).load()

        assertEquals(CoachEndpointMode.Custom, restored.endpointConfig.mode)
        assertEquals("https://demo.trycloudflare.com", restored.endpointConfig.baseUrl)
    }

    @Test
    fun persistsEngineSelectionAcrossStoreInstances() {
        val storage = FakeAppSettingsStorage()
        val offlineConfig = EngineSelectionConfig.default().useOfflineFirst()

        AppSettingsStore(storage).saveEngineSelectionConfig(offlineConfig)
        val restored = AppSettingsStore(storage).load()

        assertEquals(EngineProfileMode.OfflineFirst, restored.engineSelectionConfig.profile)
        assertEquals(EvaluationEngineMode.LocalRules, restored.engineSelectionConfig.evaluationEngine)
    }

    @Test
    fun invalidPersistedValuesFallBackToDefaults() {
        val storage = FakeAppSettingsStorage(
            "endpoint.mode" to "MissingMode",
            "engine.profile" to "MissingProfile"
        )

        val restored = AppSettingsStore(storage).load()

        assertEquals(CoachEndpointConfig.default(), restored.endpointConfig)
        assertEquals(EngineSelectionConfig.default(), restored.engineSelectionConfig)
    }

    private class FakeAppSettingsStorage(
        vararg initialValues: Pair<String, String>
    ) : AppSettingsStorage {
        private val values = mutableMapOf(*initialValues)

        override fun read(key: String): String? = values[key]

        override fun write(key: String, value: String) {
            values[key] = value
        }
    }
}
