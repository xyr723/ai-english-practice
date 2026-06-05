package com.xengineer.aienglishpractice.core

data class HomeDashboard(
    val primaryScenario: PracticeScenario,
    val practiceStats: PracticeStats,
    val quickActions: List<QuickAction>,
    val recentSummary: PracticeSummary?
) {
    companion object {
        fun default(): HomeDashboard = HomeDashboard(
            primaryScenario = ScenarioCatalog.recommended(),
            practiceStats = PracticeStats(
                todayGoalMinutes = 12,
                completedTurns = 0,
                streakDays = 1
            ),
            quickActions = listOf(
                QuickAction("Browse scenarios", AppRoute.Scenarios),
                QuickAction("History", AppRoute.History),
                QuickAction("Settings", AppRoute.Settings)
            ),
            recentSummary = null
        )
    }
}

data class PracticeStats(
    val todayGoalMinutes: Int,
    val completedTurns: Int,
    val streakDays: Int
)

data class QuickAction(
    val label: String,
    val route: AppRoute
)
