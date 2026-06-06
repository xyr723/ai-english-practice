package com.xengineer.aienglishpractice.ui.practice

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeScreenContractTest {
    @Test
    fun practiceControlsDoNotExposeDeveloperOnlyActions() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertFalse(source.contains("模拟异常"))
        assertFalse(source.contains("仅后端"))
    }

    @Test
    fun practiceScreenRendersLottieCharacterModule() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("CoachCharacterPanel"))
        assertTrue(source.contains("CoachCharacterState.from"))
    }

    @Test
    fun lottieCharacterAssetsCoverFourRequiredStates() {
        val rawDir = File("src/main/res/raw")

        assertTrue(File(rawDir, "coach_idle.json").exists())
        assertTrue(File(rawDir, "coach_listening.json").exists())
        assertTrue(File(rawDir, "coach_thinking.json").exists())
        assertTrue(File(rawDir, "coach_speaking.json").exists())
    }

    @Test
    fun lottieComposeDependencyIsDeclared() {
        val source = File("../build.gradle.kts").readText() +
            File("build.gradle.kts").readText()

        assertTrue(source.contains("com.airbnb.android:lottie-compose"))
    }
}
