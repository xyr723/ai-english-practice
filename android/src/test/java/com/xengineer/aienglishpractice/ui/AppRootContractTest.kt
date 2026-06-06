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

        assertTrue(source.contains("EngineSelectionConfig.default()"))
        assertTrue(source.contains("engineSelectionConfig = engineSelectionConfig"))
        assertTrue(source.contains("onEngineSelectionChange = { engineSelectionConfig = it }"))
    }

    @Test
    fun practiceScreenReceivesEngineSelectionConfig() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/AppRoot.kt").readText()

        assertTrue(source.contains("PracticeScreen("))
        assertTrue(source.contains("coachBaseUrl = endpointConfig.baseUrl,\n            engineSelectionConfig = engineSelectionConfig"))
    }
}
