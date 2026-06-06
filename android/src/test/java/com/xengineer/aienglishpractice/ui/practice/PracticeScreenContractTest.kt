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
    fun practiceScreenUsesDedicatedEnhancedSummaryPage() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("EnhancedSummaryPage"))
        assertTrue(source.contains("scoreBreakdown"))
        assertTrue(source.contains("turnReviews"))
        assertTrue(source.contains("practicePlan"))
    }
}
