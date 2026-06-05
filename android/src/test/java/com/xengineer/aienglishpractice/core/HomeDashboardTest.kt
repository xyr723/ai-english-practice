package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeDashboardTest {
    @Test
    fun homeDashboardHighlightsRestaurantAsPrimaryRecommendation() {
        val dashboard = HomeDashboard.default()

        assertEquals("restaurant", dashboard.primaryScenario.id)
        assertEquals("Restaurant Ordering", dashboard.primaryScenario.name)
        assertEquals(ScenarioCatalog.recommended(), dashboard.primaryScenario)
        assertTrue(dashboard.practiceStats.todayGoalMinutes > 0)
        assertTrue(dashboard.quickActions.any { it.route == AppRoute.Scenarios })
    }
}
