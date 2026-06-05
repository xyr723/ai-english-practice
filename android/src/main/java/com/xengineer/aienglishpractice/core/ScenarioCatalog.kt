package com.xengineer.aienglishpractice.core

object ScenarioCatalog {
    private val scenarios = listOf(
        PracticeScenario.restaurant(),
        PracticeScenario.interview(),
        PracticeScenario.meeting()
    )

    fun all(): List<PracticeScenario> = scenarios

    fun recommended(): PracticeScenario = scenarios.first()

    fun findById(id: String): PracticeScenario? = scenarios.firstOrNull { scenario ->
        scenario.id == id
    }
}
