package com.xengineer.aienglishpractice

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadmeContractTest {
    @Test
    fun readmeIncludesMultiEngineRoadmapHighlight() {
        val readme = File("../README.md").readText()

        assertTrue(readme.contains("多引擎切换"))
        assertTrue(readme.contains("ASR"))
        assertTrue(readme.contains("TTS"))
        assertTrue(readme.contains("LanguageTool"))
    }
}
