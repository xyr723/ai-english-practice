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
                message = "Use 'to order' or a polite ordering expression.",
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
                message = "Add a polite expression in ordering scenes.",
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
