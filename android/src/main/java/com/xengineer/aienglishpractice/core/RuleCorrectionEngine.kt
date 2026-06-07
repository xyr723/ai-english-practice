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

    private fun hasPoliteMarker(text: String): Boolean {
        val lowered = text.lowercase()
        return listOf("please", "could i", "would like", "i'd like").any { marker ->
            marker in lowered
        }
    }
}
