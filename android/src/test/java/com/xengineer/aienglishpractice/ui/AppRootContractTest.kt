package com.xengineer.aienglishpractice.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRootContractTest {
    @Test
    fun settingsEndpointIsUsedByPracticeScreen() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/AppRoot.kt").readText()

        assertTrue(source.contains("CoachSettingsScreen"))
        assertTrue(source.contains("coachBaseUrl = endpointConfig.baseUrl"))
    }

    @Test
    fun settingsReceivesEngineSelectionConfig() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/AppRoot.kt").readText()

        assertTrue(source.contains("persistedSettings.engineSelectionConfig"))
        assertTrue(source.contains("engineSelectionConfig = engineSelectionConfig"))
        assertTrue(source.contains("onEngineSelectionChange = {"))
        assertTrue(source.contains("engineSelectionConfig = it"))
    }

    @Test
    fun practiceScreenReceivesEngineSelectionConfig() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/AppRoot.kt").readText()

        assertTrue(source.contains("PracticeScreen("))
        assertTrue(source.contains("coachBaseUrl = endpointConfig.baseUrl"))
        assertTrue(source.contains("engineSelectionConfig = engineSelectionConfig"))
    }

    @Test
    fun settingsAreLoadedAndSavedThroughPersistentStore() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/AppRoot.kt").readText()

        assertTrue(source.contains("SharedPreferencesAppSettingsStorage"))
        assertTrue(source.contains("AppSettingsStore("))
        assertTrue(source.contains("settingsStore.load()"))
        assertTrue(source.contains("settingsStore.saveEndpointConfig(it)"))
        assertTrue(source.contains("settingsStore.saveEngineSelectionConfig(it)"))
    }

    @Test
    fun rootUsesPersistentPracticeHistoryStore() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/AppRoot.kt").readText()

        assertTrue(source.contains("LocalContext.current"))
        assertTrue(source.contains("SharedPreferencesPracticeHistoryStorage"))
        assertTrue(source.contains("PracticeHistoryStore(SharedPreferencesPracticeHistoryStorage(context.applicationContext))"))
    }

    @Test
    fun rootKeepsCustomScenariosAndPassesSelectedScenarioToPractice() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/AppRoot.kt").readText()

        assertTrue(source.contains("customScenarios"))
        assertTrue(source.contains("CustomScenarioFactory.fromPrompt"))
        assertTrue(source.contains("scenarioOverride ="))
    }
}
