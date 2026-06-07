package com.xengineer.aienglishpractice.ui.settings

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachSettingsScreenContractTest {
    @Test
    fun settingsScreenUsesReferenceSettingsRows() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/settings/CoachSettingsScreen.kt").readText()

        assertTrue(source.contains("SettingsOptionList"))
        assertTrue(source.contains("SettingsOptionRow"))
        assertTrue(source.contains("语速"))
        assertTrue(source.contains("深浅风格"))
    }

    @Test
    fun unavailableSettingsOpenDevelopmentDialogWithoutFutureCopy() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/settings/CoachSettingsScreen.kt").readText()

        assertFalse(source.contains("未来能力"))
        assertFalse(source.contains("即将支持"))
        assertFalse(source.contains("预留"))
        assertFalse(source.contains("AlertDialog"))
        assertTrue(source.contains("DevelopmentGlassDialog"))
        assertTrue(source.contains("此功能正在开发中"))
        assertTrue(source.contains("引擎策略"))
        assertTrue(source.contains("nextEngineSelection"))
    }

    @Test
    fun settingsContentUsesScrollableListWithoutTinyForcedRows() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/settings/CoachSettingsScreen.kt").readText()

        assertTrue(source.contains("verticalScroll"))
        assertTrue(source.contains("rememberScrollState"))
        assertTrue(source.contains(".height(56.dp)"))
        assertFalse(source.contains(".height(34.dp)"))
    }

    @Test
    fun settingsShowsCurrentEngineModelDetails() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/settings/CoachSettingsScreen.kt").readText()

        assertTrue(source.contains("EngineModelSection"))
        assertTrue(source.contains("当前 ASR"))
        assertTrue(source.contains("当前 TTS"))
        assertTrue(source.contains("当前评测模型"))
        assertTrue(source.contains("engineSelectionConfig.asrEngine.title"))
        assertTrue(source.contains("engineSelectionConfig.ttsEngine.title"))
        assertTrue(source.contains("engineSelectionConfig.evaluationEngine.title"))
    }

    @Test
    fun settingsDevelopmentDialogUsesFrostedGlassTreatment() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/settings/CoachSettingsScreen.kt").readText()

        assertTrue(source.contains("BorderStroke"))
        assertTrue(source.contains("Color.White.copy(alpha = 0.16f)"))
        assertTrue(source.contains("Color.White.copy(alpha = 0.08f)"))
    }
}
