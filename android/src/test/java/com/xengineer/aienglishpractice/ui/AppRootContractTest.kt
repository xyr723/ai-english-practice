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
}
