package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomScenarioFactoryTest {
    @Test
    fun airportRebookingPromptBuildsAirportSceneDescriptor() {
        val scenario = CustomScenarioFactory.fromPrompt("机场改签")

        assertTrue(scenario.id.startsWith("custom-airport-"))
        assertEquals("机场改签", scenario.name)
        assertEquals("航空工作人员", scenario.role)
        assertEquals("airport", scenario.sceneDescriptor.theme)
        assertEquals("#DDEEFF", scenario.sceneDescriptor.backgroundColor)
        assertEquals("airline staff", scenario.sceneDescriptor.role)
        assertTrue(scenario.sceneDescriptor.objects.containsAll(listOf("counter", "screen", "suitcase", "window")))
    }

    @Test
    fun customScenarioCanRunThroughPracticeSession() {
        val scenario = CustomScenarioFactory.fromPrompt("机场改签")
        val session = PracticeSession(
            scenario = scenario,
            correctionEngine = RuleCorrectionEngine(),
            scoreEngine = ScoreEngine()
        )

        session.start()
        val result = session.submitTurn(
            text = "I need to change my flight, please",
            durationMs = 7200,
            asrConfidence = 0.88f
        )

        assertTrue(result.reply.isNotBlank())
        assertTrue(result.betterExpression.isNotBlank())
        assertTrue(result.scores.completion.score > 0)
    }

    @Test
    fun shoppingPromptBuildsShoppingSceneDescriptor() {
        val scenario = CustomScenarioFactory.fromPrompt("购物讨价")

        assertEquals("shopping", scenario.sceneDescriptor.theme)
        assertEquals("店员", scenario.role)
        assertEquals("shop assistant", scenario.sceneDescriptor.role)
        assertTrue(scenario.sceneDescriptor.objects.containsAll(listOf("storefront", "display", "bag", "counter")))
    }

    @Test
    fun unknownPromptBuildsDistinctGeneratedSceneDescriptor() {
        val scenario = CustomScenarioFactory.fromPrompt("医院挂号")

        assertEquals("hospital", scenario.sceneDescriptor.theme)
        assertEquals("医院工作人员", scenario.role)
        assertEquals("clinic receptionist", scenario.sceneDescriptor.role)
        assertTrue(scenario.sceneDescriptor.objects.containsAll(listOf("counter", "screen", "chair", "window")))
    }

    @Test
    fun unsupportedPromptStillBuildsGeneratedCustomSceneDescriptor() {
        val scenario = CustomScenarioFactory.fromPrompt("图书馆借书")

        assertEquals("library", scenario.sceneDescriptor.theme)
        assertEquals("图书馆工作人员", scenario.role)
        assertEquals("librarian", scenario.sceneDescriptor.role)
        assertTrue(scenario.sceneDescriptor.objects.containsAll(listOf("bookshelf", "book", "card", "desk")))
        assertTrue(scenario.turns.first().reply.contains("book", ignoreCase = true))
    }

    @Test
    fun unsupportedPromptKeepsPromptTopicInGeneratedDialogue() {
        val scenario = CustomScenarioFactory.fromPrompt("健身房办卡")

        assertEquals("custom", scenario.sceneDescriptor.theme)
        assertTrue(scenario.opening.contains("健身房办卡"))
        assertTrue(scenario.turns.first().reply.contains("健身房办卡"))
        assertTrue(scenario.fallbackReplies.first().contains("健身房办卡"))
    }
}
