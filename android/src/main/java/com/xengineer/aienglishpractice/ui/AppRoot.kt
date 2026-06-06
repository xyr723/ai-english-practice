package com.xengineer.aienglishpractice.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.xengineer.aienglishpractice.core.AppNavigator
import com.xengineer.aienglishpractice.core.AppRoute
import com.xengineer.aienglishpractice.core.CoachEndpointConfig
import com.xengineer.aienglishpractice.core.EngineSelectionConfig
import com.xengineer.aienglishpractice.core.HomeDashboard
import com.xengineer.aienglishpractice.core.LocalPracticeHistory
import com.xengineer.aienglishpractice.core.ScenarioCatalog
import com.xengineer.aienglishpractice.ui.history.HistoryScreen
import com.xengineer.aienglishpractice.ui.home.HomeScreen
import com.xengineer.aienglishpractice.ui.practice.PracticeScreen
import com.xengineer.aienglishpractice.ui.scenario.ScenarioDetailScreen
import com.xengineer.aienglishpractice.ui.scenario.ScenarioListScreen
import com.xengineer.aienglishpractice.ui.settings.CoachSettingsScreen

@Composable
fun AppRoot() {
    val navigator = remember { AppNavigator() }
    var route by remember { mutableStateOf(navigator.currentRoute) }
    var historyRevision by remember { mutableStateOf(0) }
    var endpointConfig by remember { mutableStateOf(CoachEndpointConfig.default()) }
    var engineSelectionConfig by remember { mutableStateOf(EngineSelectionConfig.default()) }
    val historyStore = LocalPracticeHistory.store

    fun navigate(action: AppNavigator.() -> Unit) {
        navigator.action()
        route = navigator.currentRoute
    }

    when (val current = route) {
        AppRoute.Home -> {
            historyRevision
            HomeScreen(
                dashboard = HomeDashboard.default(historyStore = historyStore),
                onStartPractice = { scenarioId -> navigate { startPractice(scenarioId) } },
                onOpenScenarios = { navigate { openScenarios() } },
                onOpenHistory = { navigate { openHistory() } },
                onOpenSettings = { navigate { openSettings() } }
            )
        }

        is AppRoute.Practice -> PracticeScreen(
            scenarioId = current.scenarioId,
            coachBaseUrl = endpointConfig.baseUrl,
            onBackHome = { navigate { goHome() } },
            onSessionFinished = { entry ->
                historyStore.record(entry)
                historyRevision += 1
            }
        )

        AppRoute.Scenarios -> ScenarioListScreen(
            scenarios = ScenarioCatalog.all(),
            onOpenDetail = { scenarioId -> navigate { openScenarioDetail(scenarioId) } },
            onBackHome = { navigate { goHome() } }
        )

        is AppRoute.ScenarioDetail -> ScenarioDetailScreen(
            scenario = ScenarioCatalog.findById(current.scenarioId) ?: ScenarioCatalog.recommended(),
            onStartPractice = { scenarioId -> navigate { startPractice(scenarioId) } },
            onBackList = { navigate { openScenarios() } }
        )

        AppRoute.History -> {
            historyRevision
            HistoryScreen(
                entries = historyStore.recent(),
                onStartPractice = { scenarioId -> navigate { startPractice(scenarioId) } },
                onClearHistory = {
                    historyStore.clear()
                    historyRevision += 1
                },
                onBackHome = { navigate { goHome() } }
            )
        }

        AppRoute.Settings -> CoachSettingsScreen(
            endpointConfig = endpointConfig,
            onEndpointConfigChange = { endpointConfig = it },
            engineSelectionConfig = engineSelectionConfig,
            onEngineSelectionChange = { engineSelectionConfig = it },
            onBackHome = { navigate { goHome() } }
        )
    }
}
