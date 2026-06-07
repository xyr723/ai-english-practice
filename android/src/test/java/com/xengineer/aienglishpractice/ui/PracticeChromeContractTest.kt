package com.xengineer.aienglishpractice.ui.shared

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeChromeContractTest {
    @Test
    fun stageScaffoldUsesAStableBackgroundDrawableInsteadOfDecorativeRings() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/shared/PracticeChrome.kt").readText()

        assertTrue(source.contains("R.drawable.bg_restaurant_photo"))
        assertTrue(source.contains("painterResource"))
        assertFalse(source.contains("CafeStageBackground"))
        assertFalse(source.contains("drawCircle("))
        assertTrue(File("src/main/res/drawable-nodpi/bg_restaurant_photo.png").exists())
    }

    @Test
    fun translucentPanelsUseFrostedGlassTreatment() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/shared/PracticeChrome.kt").readText()

        assertTrue(source.contains("BorderStroke"))
        assertTrue(source.contains("FrostedPanelTint"))
        assertTrue(source.contains("FrostedPanelHighlight"))
        assertTrue(source.contains("FrostedPanelBorder"))
    }
}
