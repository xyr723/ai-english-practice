package com.xengineer.aienglishpractice.core

class RuleCorrectionEngine {
    fun check(text: String, scenario: PracticeScenario): CorrectionResult {
        val original = text.trim()
        var better = original
        val issues = mutableListOf<CorrectionIssue>()

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

    private fun hasPoliteMarker(text: String): Boolean {
        val lowered = text.lowercase()
        return listOf("please", "could i", "would like", "i'd like").any { marker ->
            marker in lowered
        }
    }
}
