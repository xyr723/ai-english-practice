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
    val fallbackReplies: List<String>,
    val description: String = "",
    val level: String = "A2-B1",
    val estimatedMinutes: Int = 8,
    val sceneTone: String = "guided"
) {
    companion object {
        fun restaurant(): PracticeScenario = PracticeScenario(
            id = "restaurant",
            name = "餐厅点餐",
            role = "服务员",
            opening = "Welcome! What would you like to order today?",
            description = "练习在咖啡店点餐，重点是礼貌表达和清晰回应。",
            level = "A2",
            estimatedMinutes = 8,
            sceneTone = "温和服务场景",
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

        fun interview(): PracticeScenario = PracticeScenario(
            id = "interview",
            name = "面试练习",
            role = "面试官",
            opening = "Thanks for joining today. Could you briefly introduce yourself?",
            description = "练习常见面试问题，用简洁、自信、专业的英语作答。",
            level = "A2-B1",
            estimatedMinutes = 10,
            sceneTone = "正式面试训练",
            goals = listOf(
                "introduce_self",
                "describe_experience",
                "answer_follow_up_question"
            ),
            keywords = listOf("experience", "project", "team", "strength", "learn"),
            turns = listOf(
                ScenarioTurn(
                    id = "turn-1",
                    expectedIntent = "introduce_self",
                    reply = "Good. Could you tell me about one project you worked on?"
                ),
                ScenarioTurn(
                    id = "turn-2",
                    expectedIntent = "describe_experience",
                    reply = "What was your main contribution to that project?"
                ),
                ScenarioTurn(
                    id = "turn-3",
                    expectedIntent = "answer_follow_up_question",
                    reply = "Thanks. What would you like to improve next?"
                )
            ),
            fallbackReplies = listOf(
                "Please answer with one concrete example.",
                "Could you make your answer a little more specific?"
            )
        )

        fun meeting(): PracticeScenario = PracticeScenario(
            id = "meeting",
            name = "会议讨论",
            role = "项目负责人",
            opening = "Let's review the plan. What should we discuss first?",
            description = "练习表达观点、追问细节，并在会议中确认下一步。",
            level = "B1",
            estimatedMinutes = 10,
            sceneTone = "协作会议场景",
            goals = listOf(
                "give_opinion",
                "ask_clarifying_question",
                "confirm_next_step"
            ),
            keywords = listOf("timeline", "risk", "plan", "next step", "agree"),
            turns = listOf(
                ScenarioTurn(
                    id = "turn-1",
                    expectedIntent = "give_opinion",
                    reply = "Thanks. What is the main timeline risk?"
                ),
                ScenarioTurn(
                    id = "turn-2",
                    expectedIntent = "ask_clarifying_question",
                    reply = "That makes sense. What next step should we assign?"
                ),
                ScenarioTurn(
                    id = "turn-3",
                    expectedIntent = "confirm_next_step",
                    reply = "Great. I will note that as the action item."
                )
            ),
            fallbackReplies = listOf(
                "Could you share one concrete point for the meeting?",
                "Please explain the risk or next step in one sentence."
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
    val tips: List<String>,
    val source: CoachFeedbackSource = CoachFeedbackSource.LocalFallback
)

data class PracticeSummary(
    val turnCount: Int,
    val averageScore: Int,
    val strengths: List<String>,
    val improvements: List<String>,
    val nextGoal: String
)
