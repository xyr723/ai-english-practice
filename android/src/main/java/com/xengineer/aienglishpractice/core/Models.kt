package com.xengineer.aienglishpractice.core

enum class PracticeState {
    Idle,
    Listening,
    Recognizing,
    Thinking,
    Speaking,
    Finished,
    Error
}

data class PracticeScenario(
    val id: String,
    val name: String,
    val role: String,
    val opening: String,
    val goals: List<String>,
    val keywords: List<String>,
    val turns: List<ScenarioTurn>,
    val fallbackReplies: List<String>
) {
    companion object {
        fun restaurant(): PracticeScenario = PracticeScenario(
            id = "restaurant",
            name = "Restaurant Ordering",
            role = "Waitress",
            opening = "Welcome! What would you like to order today?",
            goals = listOf(
                "order_food_or_drink",
                "use_polite_expression",
                "answer_follow_up_question"
            ),
            keywords = listOf("coffee", "tea", "sandwich", "takeaway", "please"),
            turns = listOf(
                ScenarioTurn(
                    id = "turn-1",
                    expectedIntent = "order_food_or_drink",
                    reply = "Sure. Would you like anything to drink?"
                ),
                ScenarioTurn(
                    id = "turn-2",
                    expectedIntent = "answer_follow_up_question",
                    reply = "Great. Is that for here or takeaway?"
                ),
                ScenarioTurn(
                    id = "turn-3",
                    expectedIntent = "confirm_option",
                    reply = "No problem. Your order will be ready soon."
                )
            ),
            fallbackReplies = listOf(
                "Could you say that again, please?",
                "Please tell me what you would like to order."
            )
        )
    }
}

data class ScenarioTurn(
    val id: String,
    val expectedIntent: String,
    val reply: String
)

data class CorrectionResult(
    val original: String,
    val betterExpression: String,
    val issues: List<CorrectionIssue>,
    val source: String = "RULE_ONLY"
)

data class CorrectionIssue(
    val type: String,
    val message: String,
    val suggestion: String
)

data class ScoreDetail(
    val score: Int,
    val reason: String
)

data class ScoreBundle(
    val grammar: ScoreDetail,
    val fluency: ScoreDetail,
    val pronunciation: ScoreDetail,
    val completion: ScoreDetail
)

data class TurnResult(
    val userText: String,
    val betterExpression: String,
    val reply: String,
    val scores: ScoreBundle,
    val tips: List<String>
)

data class PracticeSummary(
    val turnCount: Int,
    val averageScore: Int,
    val strengths: List<String>,
    val improvements: List<String>,
    val nextGoal: String
)
