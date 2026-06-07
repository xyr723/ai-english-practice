package com.xengineer.aienglishpractice.core

sealed interface AppRoute {
    data object Home : AppRoute
    data object Scenarios : AppRoute
    data object History : AppRoute
    data object Settings : AppRoute
    data class ScenarioDetail(val scenarioId: String) : AppRoute
    data class Practice(val scenarioId: String) : AppRoute
    data class HistoryReview(val entryId: String) : AppRoute
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

    fun openScenarioDetail(scenarioId: String) {
        currentRoute = AppRoute.ScenarioDetail(scenarioId)
    }

    fun openHistory() {
        currentRoute = AppRoute.History
    }

    fun openHistoryReview(entryId: String) {
        currentRoute = AppRoute.HistoryReview(entryId)
    }

    fun openSettings() {
        currentRoute = AppRoute.Settings
    }

    fun startPractice(scenarioId: String) {
        currentRoute = AppRoute.Practice(scenarioId)
    }
}
