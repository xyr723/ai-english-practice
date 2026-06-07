package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Test

class CoachFeedbackSourceTest {
    @Test
    fun fromBackendSourceMapsKnownCorrectionSources() {
        assertEquals(
            CoachFeedbackSource.LanguageTool,
            CoachFeedbackSource.fromBackendSource("LANGUAGE_TOOL")
        )
        assertEquals(
            CoachFeedbackSource.BackendRuleFallback,
            CoachFeedbackSource.fromBackendSource("RULE_FALLBACK")
        )
        assertEquals(
            CoachFeedbackSource.BackendRule,
            CoachFeedbackSource.fromBackendSource("RULE_ONLY")
        )
    }

    @Test
    fun fromBackendSourceUsesGenericBackendForUnknownSource() {
        assertEquals(
            CoachFeedbackSource.BackendApi,
            CoachFeedbackSource.fromBackendSource("UNEXPECTED")
        )
    }
}
