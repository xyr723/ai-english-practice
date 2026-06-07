package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavigatorTest {
    @Test
    fun navigatorStartsAtHome() {
        val navigator = AppNavigator()

        assertEquals(AppRoute.Home, navigator.currentRoute)
    }

    @Test
    fun startRecommendedPracticeOpensRestaurantPractice() {
        val navigator = AppNavigator()

        navigator.startPractice("restaurant")

        assertEquals(AppRoute.Practice("restaurant"), navigator.currentRoute)
    }

    @Test
    fun secondaryHomeDestinationsCanReturnHome() {
        val navigator = AppNavigator()

        navigator.openScenarios()
        assertEquals(AppRoute.Scenarios, navigator.currentRoute)

        navigator.openHistory()
        assertEquals(AppRoute.History, navigator.currentRoute)

        navigator.goHome()
        assertEquals(AppRoute.Home, navigator.currentRoute)
    }

    @Test
    fun scenarioDetailCanStartSelectedPractice() {
        val navigator = AppNavigator()

        navigator.openScenarioDetail("meeting")
        assertEquals(AppRoute.ScenarioDetail("meeting"), navigator.currentRoute)

        navigator.startPractice("meeting")
        assertEquals(AppRoute.Practice("meeting"), navigator.currentRoute)
    }

    @Test
    fun historyEntryCanOpenReviewRoute() {
        val navigator = AppNavigator()

        navigator.openHistoryReview("entry-1")

        assertEquals(AppRoute.HistoryReview("entry-1"), navigator.currentRoute)
    }
}
