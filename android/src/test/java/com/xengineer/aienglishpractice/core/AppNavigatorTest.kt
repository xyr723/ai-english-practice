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
}
