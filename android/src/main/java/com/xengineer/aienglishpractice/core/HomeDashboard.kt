package com.xengineer.aienglishpractice.core

data class HomeDashboard(
    val primaryScenario: PracticeScenario,
    val practiceStats: PracticeStats,
    val quickActions: List<QuickAction>,
    val recentSummary: PracticeSummary?,
    val recentHistory: PracticeHistoryEntry? = null
) {
    companion object {
        fun default(historyStore: PracticeHistoryStore = LocalPracticeHistory.store): HomeDashboard {
            val latestHistory = historyStore.latest()

            return HomeDashboard(
                primaryScenario = ScenarioCatalog.recommended(),
                practiceStats = PracticeStats(
                    todayGoalMinutes = 12,
                    completedTurns = historyStore.totalTurns(),
                    streakDays = 1
                ),
                quickActions = listOf(
                    QuickAction("浏览场景", AppRoute.Scenarios),
                    QuickAction("练习记录", AppRoute.History),
                    QuickAction("设置", AppRoute.Settings)
                ),
                recentSummary = latestHistory?.toSummary(),
                recentHistory = latestHistory
            )
        }
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
