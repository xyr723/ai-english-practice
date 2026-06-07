package com.xengineer.aienglishpractice

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryApiClientContractTest {
    @Test
    fun summaryApiClientPostsTurnsToBackendSummaryEndpoint() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/network/SummaryApiClient.kt").readText()

        assertTrue(source.contains("/summary"))
        assertTrue(source.contains("SummaryPayload"))
        assertTrue(source.contains("turns"))
        assertTrue(source.contains("scoreJson"))
        assertTrue(source.contains("toPracticeSummary"))
        assertTrue(source.contains("CoachFeedbackSource.fromBackendSource"))
    }
}
