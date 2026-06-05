package com.xengineer.aienglishpractice.core

class PracticeSession(
    private val scenario: PracticeScenario,
    private val correctionEngine: RuleCorrectionEngine,
    private val scoreEngine: ScoreEngine
) {
    var state: PracticeState = PracticeState.Idle
        private set

    private val turns = mutableListOf<TurnResult>()

    val turnCount: Int
        get() = turns.size

    fun start() {
        state = PracticeState.Idle
    }

    fun submitTurn(
        text: String,
        durationMs: Int,
        asrConfidence: Float?
    ): TurnResult {
        state = PracticeState.Thinking

        val correction = correctionEngine.check(text, scenario)
        val matchedGoals = matchedGoals(text)
        val scores = scoreEngine.score(
            text = text,
            durationMs = durationMs,
            asrConfidence = asrConfidence,
            correction = correction,
            matchedGoals = matchedGoals.size,
            totalGoals = scenario.goals.size
        )
        val result = TurnResult(
            userText = text,
            betterExpression = correction.betterExpression,
            reply = nextReply(),
            replyTranslation = nextReplyTranslation(),
            scores = scores,
            tips = correction.issues.map { it.message }
        )

        turns += result
        state = PracticeState.Speaking
        return result
    }

    fun recordAnalyzedTurn(result: TurnResult): TurnResult {
        turns += result
        state = PracticeState.Speaking
        return result
    }

    fun finish(): PracticeSummary {
        state = PracticeState.Finished
        val averages = turns.map { turn ->
            listOf(
                turn.scores.grammar.score,
                turn.scores.fluency.score,
                turn.scores.pronunciation.score,
                turn.scores.completion.score
            ).average().toInt()
        }
        val averageScore = if (averages.isEmpty()) 0 else averages.average().toInt()

        return PracticeSummary(
            turnCount = turns.size,
            averageScore = averageScore,
            strengths = if (turns.isEmpty()) emptyList() else listOf("你完成了本次场景练习。"),
            improvements = listOf("复习优化表达，并重复练习同一场景。"),
            nextGoal = "下次重点练习确认价格和外带选项。"
        )
    }

    private fun nextReply(): String {
        val index = turns.size
        return scenario.turns.getOrNull(index)?.reply
            ?: scenario.fallbackReplies.firstOrNull()
            ?: "Could you say that again, please?"
    }

    private fun nextReplyTranslation(): String {
        val index = turns.size
        return scenario.turns.getOrNull(index)?.replyTranslation
            ?: scenario.fallbackReplyTranslations.firstOrNull()
            ?: ""
    }

    private fun matchedGoals(text: String): List<String> {
        val lowered = text.lowercase()
        val matched = linkedSetOf<String>()

        if (
            "order_food_or_drink" in scenario.goals &&
            scenario.keywords.any { keyword -> keyword.lowercase() in lowered }
        ) {
            matched += "order_food_or_drink"
        }

        if (
            "use_polite_expression" in scenario.goals &&
            listOf("please", "could i", "would like", "i'd like").any { it in lowered }
        ) {
            matched += "use_polite_expression"
        }

        if (
            "answer_follow_up_question" in scenario.goals &&
            listOf("yes", "no", "for here", "takeaway", "to go").any { it in lowered }
        ) {
            matched += "answer_follow_up_question"
        }

        scenario.goals.forEach { goal ->
            if (matchesGoal(goal, lowered)) {
                matched += goal
            }
        }

        if (matched.isEmpty() && scenario.keywords.any { keyword -> keyword.lowercase() in lowered }) {
            matched += scenario.goals.first()
        }

        return matched.toList()
    }

    private fun matchesGoal(goal: String, loweredText: String): Boolean {
        val markers = when (goal) {
            "introduce_self" -> listOf("my name", "i am", "i'm", "student", "developer")
            "describe_experience" -> listOf("experience", "project", "worked", "built", "team")
            "give_opinion" -> listOf("i think", "i believe", "should", "agree")
            "ask_clarifying_question" -> listOf("?", "what", "how", "could you", "risk")
            "confirm_next_step" -> listOf("next step", "assign", "will", "follow up", "plan")
            else -> emptyList()
        }

        return markers.any { marker -> marker in loweredText }
    }
}
