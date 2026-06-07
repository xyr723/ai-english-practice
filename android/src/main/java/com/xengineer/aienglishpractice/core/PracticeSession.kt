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

    fun recordedTurns(): List<TurnResult> = turns.toList()

    fun start() {
        state = PracticeState.Idle
    }

    fun submitTurn(
        text: String,
        durationMs: Int,
        asrConfidence: Float?,
        recognitionAlternatives: List<RecognitionAlternative> = emptyList()
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
            totalGoals = scenario.goals.size,
            recognitionAlternatives = recognitionAlternatives
        )
        val replyTurn = nextReplyTurn(text)
        val result = TurnResult(
            userText = text,
            betterExpression = correction.betterExpression,
            reply = replyTurn?.reply ?: nextReply(),
            replyTranslation = replyTurn?.replyTranslation ?: nextReplyTranslation(),
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
            turn.averageScore()
        }
        val averageScore = if (averages.isEmpty()) 0 else averages.average().toInt()
        val scoreBreakdown = summaryScoreBreakdown()
        val turnReviews = turns.mapIndexed { index, turn ->
            SummaryTurnReview(
                index = index + 1,
                userText = turn.userText,
                betterExpression = turn.betterExpression,
                reply = turn.reply,
                score = turn.averageScore(),
                tips = turn.tips
            )
        }

        return PracticeSummary(
            turnCount = turns.size,
            averageScore = averageScore,
            strengths = strengths(scoreBreakdown),
            improvements = improvements(scoreBreakdown),
            nextGoal = nextGoal(),
            scoreBreakdown = scoreBreakdown,
            turnReviews = turnReviews,
            practicePlan = practicePlan()
        )
    }

    private fun summaryScoreBreakdown(): List<SummaryScoreBreakdown> {
        if (turns.isEmpty()) {
            return listOf(
                SummaryScoreBreakdown("语法", 0, "还没有完成练习轮次。"),
                SummaryScoreBreakdown("流利度", 0, "还没有完成练习轮次。"),
                SummaryScoreBreakdown("发音", 0, "还没有完成练习轮次。"),
                SummaryScoreBreakdown("完成度", 0, "还没有完成练习轮次。"),
                SummaryScoreBreakdown("词汇", 0, "还没有完成练习轮次。")
            )
        }

        return listOf(
            SummaryScoreBreakdown("语法", turns.averageOf { it.scores.grammar.score }, "表达准确度和纠错数量。"),
            SummaryScoreBreakdown("流利度", turns.averageOf { it.scores.fluency.score }, "回答速度和连续表达稳定性。"),
            SummaryScoreBreakdown("发音", turns.averageOf { it.scores.pronunciation.score }, "语音识别置信度代表的清晰度。"),
            SummaryScoreBreakdown("完成度", turns.averageOf { it.scores.completion.score }, "场景目标命中情况。"),
            SummaryScoreBreakdown("词汇", turns.averageOf { it.vocabularyScore() }, "表达丰富度和场景关键词使用。")
        )
    }

    private fun strengths(scoreBreakdown: List<SummaryScoreBreakdown>): List<String> {
        if (turns.isEmpty()) return emptyList()
        val best = scoreBreakdown.maxByOrNull { it.score }
        return listOf(
            "你完成了 ${turns.size} 轮 ${scenario.name} 练习。",
            "本次表现最稳定的是${best?.label ?: "完成度"}，平均 ${best?.score ?: 0} 分。"
        )
    }

    private fun improvements(scoreBreakdown: List<SummaryScoreBreakdown>): List<String> {
        val weakest = scoreBreakdown.minByOrNull { it.score }
        val correctionTip = turns.firstOrNull { it.tips.isNotEmpty() }?.tips?.firstOrNull()
            ?: "复习优化表达，并重复练习同一场景。"
        return listOf(
            "优先提升${weakest?.label ?: "表达稳定性"}，当前平均 ${weakest?.score ?: 0} 分。",
            correctionTip
        )
    }

    private fun nextGoal(): String = when (scenario.id) {
        "restaurant" -> "下次重点练习确认价格、饮品和外带选项。"
        "interview" -> "下次用 STAR 结构补充一个项目案例。"
        "meeting" -> "下次练习先表达观点，再确认风险和下一步。"
        else -> "下次选择同一场景，完成更完整的三轮对话。"
    }

    private fun practicePlan(): List<String> = listOf(
        "先朗读本次 better expression 3 遍。",
        "用同一场景再完成至少 2 轮回答。",
        "下一轮刻意使用 1 个礼貌表达和 1 个追问句。"
    )

    private fun nextReply(): String {
        val index = turns.size
        return scenario.turns.getOrNull(index)?.reply
            ?: completionReply()
    }

    private fun nextReplyTurn(text: String): ScenarioTurn? {
        if (scenario.id != "restaurant" || turns.isNotEmpty()) return null

        val lowered = text.lowercase()
        val hasDrink = listOf("tea", "coffee", "water", "juice", "drink", "cola").any { it in lowered }
        val hasFood = listOf("hamburger", "burger", "sandwich", "cake", "salad", "food").any { it in lowered }
        if (hasFood && hasDrink) {
            return scenario.turns.getOrNull(1)
        }

        return null
    }

    private fun nextReplyTranslation(): String {
        val index = turns.size
        return scenario.turns.getOrNull(index)?.replyTranslation
            ?: completionReplyTranslation()
    }

    private fun completionReply(): String =
        "Great. That completes this practice. Try one more answer with a specific detail."

    private fun completionReplyTranslation(): String =
        "很好。本次练习已完成。下一次回答时再补充一个具体细节。"

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

private fun TurnResult.averageScore(): Int = listOf(
    scores.grammar.score,
    scores.fluency.score,
    scores.pronunciation.score,
    scores.completion.score
).average().toInt()

private fun TurnResult.vocabularyScore(): Int =
    ((scores.grammar.score + scores.completion.score) / 2).coerceIn(0, 100)

private inline fun List<TurnResult>.averageOf(score: (TurnResult) -> Int): Int =
    map(score).average().toInt()
