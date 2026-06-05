package com.xengineer.aienglishpractice.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.xengineer.aienglishpractice.core.AppNavigator
import com.xengineer.aienglishpractice.core.AppRoute
import com.xengineer.aienglishpractice.core.HomeDashboard
import com.xengineer.aienglishpractice.ui.home.HomeScreen
import com.xengineer.aienglishpractice.ui.practice.PracticeScreen
import com.xengineer.aienglishpractice.ui.shared.PlaceholderScreen

@Composable
fun AppRoot() {
    val navigator = remember { AppNavigator() }
    var route by remember { mutableStateOf(navigator.currentRoute) }

    fun navigate(action: AppNavigator.() -> Unit) {
        navigator.action()
        route = navigator.currentRoute
    }

    when (val current = route) {
        AppRoute.Home -> HomeScreen(
            dashboard = HomeDashboard.default(),
            onStartPractice = { scenarioId -> navigate { startPractice(scenarioId) } },
            onOpenScenarios = { navigate { openScenarios() } },
            onOpenHistory = { navigate { openHistory() } },
            onOpenSettings = { navigate { openSettings() } }
        )

        is AppRoute.Practice -> PracticeScreen(
            scenarioId = current.scenarioId,
            onBackHome = { navigate { goHome() } }
        )

        AppRoute.Scenarios -> PlaceholderScreen(
            title = "Scenarios",
            body = "Restaurant is ready. Interview and meeting scenarios are next.",
            action = "Back home",
            onAction = { navigate { goHome() } }
        )

        AppRoute.History -> PlaceholderScreen(
            title = "Practice history",
            body = "Completed sessions will appear here after local history storage is added.",
            action = "Back home",
            onAction = { navigate { goHome() } }
        )

        AppRoute.Settings -> PlaceholderScreen(
            title = "Settings",
            body = "Voice, backend, and display options will be collected here.",
            action = "Back home",
            onAction = { navigate { goHome() } }
        )
    }
}
