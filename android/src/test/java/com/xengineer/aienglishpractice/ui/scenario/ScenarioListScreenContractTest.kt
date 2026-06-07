package com.xengineer.aienglishpractice.ui.scenario

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioListScreenContractTest {
    @Test
    fun scenarioListExposesCustomScenePromptEntry() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/scenario/ScenarioListScreen.kt").readText()

        assertTrue(source.contains("onCreateCustomScenario"))
        assertTrue(source.contains("CustomScenePromptPanel"))
        assertTrue(source.contains("请输入自定义场景"))
        assertTrue(source.contains("生成场景"))
        assertTrue(source.contains("OutlinedTextFieldDefaults.colors"))
        assertTrue(source.contains("focusedTextColor = Color.White"))
    }

    @Test
    fun scenarioPagesUseDarkTranslucentThemeInsteadOfWhitePanels() {
        val listSource = File("src/main/java/com/xengineer/aienglishpractice/ui/scenario/ScenarioListScreen.kt").readText()
        val detailSource = File("src/main/java/com/xengineer/aienglishpractice/ui/scenario/ScenarioDetailScreen.kt").readText()

        assertTrue(listSource.contains("GlassPanel"))
        assertTrue(detailSource.contains("GlassPanel"))
        assertTrue(listSource.contains("painterResource"))
        assertTrue(detailSource.contains("scenarioIconFor"))
        assertTrue(listSource.contains("R.drawable.ic_custom_scene"))
        assertTrue(listSource.contains("R.drawable.ic_arrow_right"))
        assertTrue(listSource.contains("ScenarioHeaderAction"))
        assertTrue(listSource.contains("R.drawable.ic_home"))
        assertTrue(listSource.contains("verticalScroll(rememberScrollState())"))
    }
}
