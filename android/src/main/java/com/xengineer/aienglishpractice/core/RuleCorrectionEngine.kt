package com.xengineer.aienglishpractice.core

class RuleCorrectionEngine {
    fun check(text: String, scenario: PracticeScenario): CorrectionResult {
        val original = text.trim()
        var better = original
        val issues = mutableListOf<CorrectionIssue>()

        commonGrammarRules.forEach { rule ->
            if (rule.pattern.containsMatchIn(better)) {
                better = better.replace(rule.pattern, rule.replacement)
                issues += CorrectionIssue(
                    type = "grammar",
                    message = rule.message,
                    suggestion = rule.replacement
                )
            }
        }

        if (Regex("\\bI\\s+want\\s+order\\b", RegexOption.IGNORE_CASE).containsMatchIn(better)) {
            better = better.replace(
                Regex("\\bI\\s+want\\s+order\\b", RegexOption.IGNORE_CASE),
                "I'd like to order"
            )
            issues += CorrectionIssue(
                type = "grammar",
                message = "点餐时建议使用 “to order” 或更礼貌的表达。",
                suggestion = "I'd like to order"
            )
        }

        better = better.replace(
            Regex("\\border\\s+(coffee|tea|sandwich)\\b", RegexOption.IGNORE_CASE),
            "order a $1"
        )

        if (scenario.goals.contains("use_polite_expression") && !hasPoliteMarker(original)) {
            better = better.trim().trimEnd('.', '!', '?') + ", please."
            issues += CorrectionIssue(
                type = "politeness",
                message = "点餐场景建议补充礼貌表达。",
                suggestion = "please"
            )
        } else if (better.isNotBlank() && better.last() !in ".!?") {
            better += "."
        }

        better = applyScenarioGuidance(
            original = original,
            better = better,
            scenario = scenario,
            issues = issues
        )

        return CorrectionResult(
            original = original,
            betterExpression = better,
            issues = issues
        )
    }

    private data class GrammarRule(
        val pattern: Regex,
        val replacement: String,
        val message: String
    )

    private val commonGrammarRules = listOf(
        GrammarRule(
            pattern = Regex("\\bI\\s+has\\b", RegexOption.IGNORE_CASE),
            replacement = "I have",
            message = "主语 I 后应使用 have。"
        ),
        GrammarRule(
            pattern = Regex("\\bI\\s+am\\s+agree\\b", RegexOption.IGNORE_CASE),
            replacement = "I agree",
            message = "agree 本身是动词，不需要写成 “am agree”。"
        ),
        GrammarRule(
            pattern = Regex("\\bI\\s+want\\s+go\\b", RegexOption.IGNORE_CASE),
            replacement = "I want to go",
            message = "want 后接动词时需要使用 to。"
        )
    )

    private data class ScenarioGuidance(
        val message: String,
        val suggestion: String,
        val appendToExpression: Boolean
    )

    private fun applyScenarioGuidance(
        original: String,
        better: String,
        scenario: PracticeScenario,
        issues: MutableList<CorrectionIssue>
    ): String {
        val guidance = scenarioGuidanceFor(scenarioKey(scenario), original.lowercase()) ?: return better
        issues += CorrectionIssue(
            type = "scenario",
            message = guidance.message,
            suggestion = guidance.suggestion
        )
        if (!guidance.appendToExpression || better.contains(guidance.suggestion, ignoreCase = true)) {
            return better
        }
        return appendSentence(better, guidance.suggestion)
    }

    private fun scenarioKey(scenario: PracticeScenario): String {
        val id = scenario.id.lowercase()
        val theme = scenario.sceneDescriptor.theme.lowercase()
        val keywords = scenario.keywords.map { it.lowercase() }.toSet()
        return when {
            theme == "airport" || "airport" in id || "flight" in keywords -> "airport"
            theme == "office" || "interview" in id || "project" in keywords -> "interview"
            theme == "meeting" || "meeting" in id || "timeline" in keywords || "risk" in keywords -> "meeting"
            theme == "restaurant" || "restaurant" in id || "coffee" in keywords || "sandwich" in keywords -> "restaurant"
            theme == "shopping" || "shopping" in id || "price" in keywords || "discount" in keywords -> "shopping"
            theme == "library" || "library" in id || "book" in keywords || "borrow" in keywords -> "library"
            theme == "hospital" || "hospital" in id || "doctor" in keywords || "appointment" in keywords -> "hospital"
            else -> "custom"
        }
    }

    private fun scenarioGuidanceFor(scenarioKey: String, loweredText: String): ScenarioGuidance? = when {
        scenarioKey == "airport" && missingAny(
            loweredText,
            listOf("flight number", "flight no", "ticket number", "earlier", "later", "time")
        ) -> ScenarioGuidance(
            message = "机场改签时建议补充航班号/机票信息，并说明想改到更早还是更晚的时间。",
            suggestion = "You can add: My flight number is ___, and I prefer an earlier/later flight.",
            appendToExpression = true
        )

        scenarioKey == "interview" && missingAny(
            loweredText,
            listOf("situation", "task", "action", "result", "impact", "outcome")
        ) -> ScenarioGuidance(
            message = "面试回答建议用 STAR 结构补充情境、任务、行动和结果。",
            suggestion = "You can use STAR: situation, task, action, and result.",
            appendToExpression = true
        )

        scenarioKey == "meeting" && missingAny(
            loweredText,
            listOf("risk", "next step", "owner", "deadline", "action item")
        ) -> ScenarioGuidance(
            message = "会议表达建议同时说明 risk、负责人或 next step，方便团队推进。",
            suggestion = "You can add: The main risk is ___, and the next step is ___.",
            appendToExpression = true
        )

        scenarioKey == "restaurant" && missingAny(
            loweredText,
            listOf("for here", "takeaway", "to go", "size", "drink")
        ) -> ScenarioGuidance(
            message = "点餐时除了礼貌表达，也可以补充堂食/外带、杯型或饮品搭配。",
            suggestion = "Could I have it for here, please?",
            appendToExpression = false
        )

        scenarioKey == "shopping" && missingAny(
            loweredText,
            listOf("size", "color", "price", "discount", "try")
        ) -> ScenarioGuidance(
            message = "购物场景建议说明尺码/颜色，并询问价格、折扣或是否可以试穿。",
            suggestion = "You can add: Do you have this in my size, and is there any discount?",
            appendToExpression = true
        )

        scenarioKey == "library" && missingAny(
            loweredText,
            listOf("book", "author", "topic", "library card", "return date")
        ) -> ScenarioGuidance(
            message = "借书场景建议说明书名、作者或主题，并确认借书证和归还日期。",
            suggestion = "You can add: I am looking for a book about ___, and I have my library card.",
            appendToExpression = true
        )

        scenarioKey == "hospital" && missingAny(
            loweredText,
            listOf("appointment", "doctor", "department", "symptom", "register")
        ) -> ScenarioGuidance(
            message = "医院场景建议说明症状、科室或是否预约，方便工作人员安排下一步。",
            suggestion = "You can add: I have ___ symptoms, and I need to register for the right department.",
            appendToExpression = true
        )

        scenarioKey == "custom" && missingAny(
            loweredText,
            listOf("need", "want", "confirm", "question", "please")
        ) -> ScenarioGuidance(
            message = "自定义场景建议先说明目标，再补充一个需要确认的细节。",
            suggestion = "You can add: I need ___, and I would like to confirm ___.",
            appendToExpression = true
        )

        else -> null
    }

    private fun missingAny(text: String, markers: List<String>): Boolean =
        markers.none { marker -> marker in text }

    private fun appendSentence(text: String, sentence: String): String {
        val stripped = text.trim()
        if (stripped.isBlank()) return sentence
        val punctuated = if (stripped.last() in ".!?") stripped else "$stripped."
        return "$punctuated $sentence"
    }

    private fun hasPoliteMarker(text: String): Boolean {
        val lowered = text.lowercase()
        return listOf("please", "could i", "would like", "i'd like").any { marker ->
            marker in lowered
        }
    }
}
