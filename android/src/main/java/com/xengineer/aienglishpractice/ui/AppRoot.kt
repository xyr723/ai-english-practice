package com.xengineer.aienglishpractice.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.xengineer.aienglishpractice.core.AppNavigator
import com.xengineer.aienglishpractice.core.AppRoute
import com.xengineer.aienglishpractice.core.AppSettingsStore
import com.xengineer.aienglishpractice.core.HomeDashboard
import com.xengineer.aienglishpractice.core.PracticeHistoryStore
import com.xengineer.aienglishpractice.core.ScenarioCatalog
import com.xengineer.aienglishpractice.core.SharedPreferencesAppSettingsStorage
import com.xengineer.aienglishpractice.core.SharedPreferencesPracticeHistoryStorage
import com.xengineer.aienglishpractice.ui.history.HistoryScreen
import com.xengineer.aienglishpractice.ui.home.HomeScreen
import com.xengineer.aienglishpractice.ui.practice.PracticeScreen
import com.xengineer.aienglishpractice.ui.scenario.ScenarioDetailScreen
import com.xengineer.aienglishpractice.ui.scenario.ScenarioListScreen
import com.xengineer.aienglishpractice.ui.settings.CoachSettingsScreen

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val settingsStore = remember(context) {
        AppSettingsStore(SharedPreferencesAppSettingsStorage(context.applicationContext))
    }
    val persistedSettings = remember(settingsStore) { settingsStore.load() }
    val navigator = remember { AppNavigator() }
    var route by remember { mutableStateOf(navigator.currentRoute) }
    var historyRevision by remember { mutableStateOf(0) }
    var endpointConfig by remember { mutableStateOf(persistedSettings.endpointConfig) }
    var engineSelectionConfig by remember { mutableStateOf(persistedSettings.engineSelectionConfig) }
    val historyStore = remember(context) {
        PracticeHistoryStore(SharedPreferencesPracticeHistoryStorage(context.applicationContext))
    }

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
            engineSelectionConfig = engineSelectionConfig,
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
            onEndpointConfigChange = {
                settingsStore.saveEndpointConfig(it)
                endpointConfig = it
            },
            engineSelectionConfig = engineSelectionConfig,
            onEngineSelectionChange = {
                settingsStore.saveEngineSelectionConfig(it)
                engineSelectionConfig = it
            },
            onBackHome = { navigate { goHome() } }
        )
    }
}
