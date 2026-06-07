package com.xengineer.aienglishpractice.core

data class AppSettings(
    val endpointConfig: CoachEndpointConfig,
    val engineSelectionConfig: EngineSelectionConfig
)

interface AppSettingsStorage {
    fun read(key: String): String?

    fun write(key: String, value: String)
}

class AppSettingsStore(private val storage: AppSettingsStorage) {
    fun load(): AppSettings = AppSettings(
        endpointConfig = loadEndpointConfig(),
        engineSelectionConfig = loadEngineSelectionConfig()
    )

    fun saveEndpointConfig(config: CoachEndpointConfig) {
        storage.write(KEY_ENDPOINT_MODE, config.mode.name)
        storage.write(KEY_CUSTOM_BASE_URL, config.customBaseUrl)
    }

    fun saveEngineSelectionConfig(config: EngineSelectionConfig) {
        storage.write(KEY_ENGINE_PROFILE, config.profile.name)
    }

    private fun loadEndpointConfig(): CoachEndpointConfig {
        val mode = storage.read(KEY_ENDPOINT_MODE)?.toEnumOrNull<CoachEndpointMode>()
            ?: return CoachEndpointConfig.default()
        val customBaseUrl = storage.read(KEY_CUSTOM_BASE_URL)
            ?: CoachEndpointConfig.CUSTOM_DEFAULT_BASE_URL
        return CoachEndpointConfig(mode = mode).useCustom(customBaseUrl).copy(mode = mode)
    }

    private fun loadEngineSelectionConfig(): EngineSelectionConfig {
        return when (storage.read(KEY_ENGINE_PROFILE)?.toEnumOrNull<EngineProfileMode>()) {
            EngineProfileMode.AccuracyFirst -> EngineSelectionConfig.default().useAccuracyFirst()
            EngineProfileMode.OfflineFirst -> EngineSelectionConfig.default().useOfflineFirst()
            EngineProfileMode.StableDemo -> EngineSelectionConfig.default().useStableDemo()
            null -> EngineSelectionConfig.default()
        }
    }

    private companion object {
        const val KEY_ENDPOINT_MODE = "endpoint.mode"
        const val KEY_CUSTOM_BASE_URL = "endpoint.customBaseUrl"
        const val KEY_ENGINE_PROFILE = "engine.profile"
    }
}

private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
    enumValues<T>().firstOrNull { value -> value.name == this }
