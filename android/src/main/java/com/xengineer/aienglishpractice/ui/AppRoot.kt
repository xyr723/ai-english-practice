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
import com.xengineer.aienglishpractice.core.CustomScenarioFactory
import com.xengineer.aienglishpractice.core.HomeDashboard
import com.xengineer.aienglishpractice.core.PracticeHistoryStore
import com.xengineer.aienglishpractice.core.PracticeScenario
import com.xengineer.aienglishpractice.core.ScenarioCatalog
import com.xengineer.aienglishpractice.core.SharedPreferencesAppSettingsStorage
import com.xengineer.aienglishpractice.core.SharedPreferencesPracticeHistoryStorage
import com.xengineer.aienglishpractice.ui.history.HistoryScreen
import com.xengineer.aienglishpractice.ui.history.HistoryReviewScreen
import com.xengineer.aienglishpractice.ui.home.HomeScreen
import com.xengineer.aienglishpractice.ui.practice.PracticeScreen
import com.xengineer.aienglishpractice.ui.scenario.ScenarioDetailScreen
import com.xengineer.aienglishpractice.ui.scenario.ScenarioListScreen
import com.xengineer.aienglishpractice.ui.settings.CoachSettingsScreen
import com.xengineer.aienglishpractice.ui.shared.PlaceholderScreen

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
    var customScenarios by remember { mutableStateOf(emptyList<PracticeScenario>()) }
    val historyStore = remember(context) {
        PracticeHistoryStore(SharedPreferencesPracticeHistoryStorage(context.applicationContext))
    }

    fun navigate(action: AppNavigator.() -> Unit) {
        navigator.action()
        route = navigator.currentRoute
    }

    fun scenarioById(scenarioId: String): PracticeScenario =
        customScenarios.firstOrNull { it.id == scenarioId }
            ?: ScenarioCatalog.findById(scenarioId)
            ?: ScenarioCatalog.recommended()

    fun createCustomScenario(prompt: String) {
        val scenario = CustomScenarioFactory.fromPrompt(prompt)
        customScenarios = listOf(scenario) + customScenarios.filterNot { it.id == scenario.id }
        navigate { startPractice(scenario.id) }
    }

    when (val current = route) {
        AppRoute.Home -> {
            historyRevision
            HomeScreen(
                dashboard = HomeDashboard.default(historyStore = historyStore),
                onStartPractice = { scenarioId -> navigate { startPractice(scenarioId) } },
                onCreateCustomScenario = { prompt -> createCustomScenario(prompt) },
                onOpenScenarios = { navigate { openScenarios() } },
                onOpenHistory = { navigate { openHistory() } },
                onOpenSettings = { navigate { openSettings() } }
            )
        }

        is AppRoute.Practice -> PracticeScreen(
            scenarioId = current.scenarioId,
            scenarioOverride = customScenarios.firstOrNull { it.id == current.scenarioId },
            coachBaseUrl = endpointConfig.baseUrl,
            engineSelectionConfig = engineSelectionConfig,
            onBackHome = { navigate { goHome() } },
            onSessionFinished = { entry ->
                historyStore.record(entry)
                historyRevision += 1
            }
        )

        AppRoute.Scenarios -> ScenarioListScreen(
            scenarios = customScenarios + ScenarioCatalog.all(),
            onOpenDetail = { scenarioId -> navigate { openScenarioDetail(scenarioId) } },
            onCreateCustomScenario = { prompt -> createCustomScenario(prompt) },
            onBackHome = { navigate { goHome() } }
        )

        is AppRoute.ScenarioDetail -> ScenarioDetailScreen(
            scenario = scenarioById(current.scenarioId),
            onStartPractice = { scenarioId -> navigate { startPractice(scenarioId) } },
            onBackList = { navigate { openScenarios() } }
        )

        AppRoute.History -> {
            historyRevision
            HistoryScreen(
                entries = historyStore.recent(),
                onStartPractice = { scenarioId -> navigate { startPractice(scenarioId) } },
                onOpenReview = { entryId -> navigate { openHistoryReview(entryId) } },
                onClearHistory = {
                    historyStore.clear()
                    historyRevision += 1
                },
                onBackHome = { navigate { goHome() } }
            )
        }

        is AppRoute.HistoryReview -> {
            historyRevision
            val entry = historyStore.recent().firstOrNull { it.id == current.entryId }
            if (entry != null) {
                HistoryReviewScreen(
                    entry = entry,
                    onBackHistory = { navigate { openHistory() } }
                )
            } else {
                PlaceholderScreen(
                    title = "记录不存在",
                    body = "这条练习记录可能已经被清空。",
                    action = "返回历史",
                    onAction = { navigate { openHistory() } }
                )
            }
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
