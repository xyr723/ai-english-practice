package com.xengineer.aienglishpractice.core

sealed interface AppRoute {
    data object Home : AppRoute
    data object Scenarios : AppRoute
    data object History : AppRoute
    data object Settings : AppRoute
    data class Practice(val scenarioId: String) : AppRoute
}

class AppNavigator(initialRoute: AppRoute = AppRoute.Home) {
    var currentRoute: AppRoute = initialRoute
        private set

    fun goHome() {
        currentRoute = AppRoute.Home
    }

    fun openScenarios() {
        currentRoute = AppRoute.Scenarios
    }

    fun openHistory() {
        currentRoute = AppRoute.History
    }

    fun openSettings() {
        currentRoute = AppRoute.Settings
    }

    fun startPractice(scenarioId: String) {
        currentRoute = AppRoute.Practice(scenarioId)
    }
}
