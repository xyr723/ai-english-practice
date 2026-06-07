package com.xengineer.aienglishpractice

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachApiClientContractTest {
    @Test
    fun coachApiClientAllowsSlowCloudAnalysisBeforeFallingBackLocally() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/network/CoachApiClient.kt").readText()

        assertTrue(source.contains("private val timeoutMs: Int = 20000"))
        assertFalse(source.contains("private val timeoutMs: Int = 8000"))
        assertFalse(source.contains("private val timeoutMs: Int = 2500"))
    }
}
