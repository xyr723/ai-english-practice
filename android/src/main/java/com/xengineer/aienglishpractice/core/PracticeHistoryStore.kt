package com.xengineer.aienglishpractice.core

data class PracticeHistoryEntry(
    val id: String,
    val scenarioId: String,
    val scenarioName: String,
    val completedAtLabel: String,
    val turnCount: Int,
    val averageScore: Int,
    val strengths: List<String>,
    val improvements: List<String>,
    val nextGoal: String
) {
    fun toSummary(): PracticeSummary = PracticeSummary(
        turnCount = turnCount,
        averageScore = averageScore,
        strengths = strengths,
        improvements = improvements,
        nextGoal = nextGoal
    )

    companion object {
        fun fromSummary(
            scenario: PracticeScenario,
            summary: PracticeSummary,
            completedAtLabel: String = "刚刚",
            id: String = "${scenario.id}-${System.currentTimeMillis()}"
        ): PracticeHistoryEntry = PracticeHistoryEntry(
            id = id,
            scenarioId = scenario.id,
            scenarioName = scenario.name,
            completedAtLabel = completedAtLabel,
            turnCount = summary.turnCount,
            averageScore = summary.averageScore,
            strengths = summary.strengths,
            improvements = summary.improvements,
            nextGoal = summary.nextGoal
        )
    }
}

class PracticeHistoryStore {
    private val entries = mutableListOf<PracticeHistoryEntry>()

    fun record(entry: PracticeHistoryEntry) {
        entries.removeAll { existing -> existing.id == entry.id }
        entries.add(0, entry)
    }

    fun recent(limit: Int = 10): List<PracticeHistoryEntry> = entries.take(limit)

    fun latest(): PracticeHistoryEntry? = entries.firstOrNull()

    fun totalTurns(): Int = entries.sumOf { entry -> entry.turnCount }

    fun clear() {
        entries.clear()
    }
}

object LocalPracticeHistory {
    val store = PracticeHistoryStore()
}
