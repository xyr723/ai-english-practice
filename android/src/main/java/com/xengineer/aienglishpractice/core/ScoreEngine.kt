package com.xengineer.aienglishpractice.core

class ScoreEngine {
    fun score(
        text: String,
        durationMs: Int,
        asrConfidence: Float?,
        correction: CorrectionResult,
        matchedGoals: Int,
        totalGoals: Int
    ): ScoreBundle {
        val wordCount = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val issueCount = correction.issues.size

        return ScoreBundle(
            grammar = ScoreDetail(
                score = clamp(100 - issueCount * 8),
                reason = "Found $issueCount grammar or expression issues."
            ),
            fluency = ScoreDetail(
                score = scoreFluency(wordCount, durationMs),
                reason = fluencyReason(wordCount, durationMs)
            ),
            pronunciation = ScoreDetail(
                score = clamp(((asrConfidence ?: 0f) * 100).toInt()),
                reason = if (asrConfidence == null) {
                    "ASR confidence is unavailable."
                } else {
                    "ASR confidence is %.2f.".format(asrConfidence)
                }
            ),
            completion = ScoreDetail(
                score = if (totalGoals <= 0) 0 else clamp((matchedGoals * 100f / totalGoals).toInt()),
                reason = "Matched $matchedGoals of $totalGoals scene goals."
            )
        )
    }

    private fun scoreFluency(wordCount: Int, durationMs: Int): Int {
        if (wordCount <= 0 || durationMs <= 0) return 0
        val wpm = wordCount / (durationMs / 60000f)
        return when {
            wpm in 80f..140f -> 100
            wpm < 80f -> clamp((100 - (80f - wpm) * 2).toInt())
            else -> clamp((100 - (wpm - 140f) * 1.5f).toInt())
        }
    }

    private fun fluencyReason(wordCount: Int, durationMs: Int): String {
        if (wordCount <= 0 || durationMs <= 0) return "No usable speaking duration was provided."
        val wpm = wordCount / (durationMs / 60000f)
        return "Speaking speed is %.0f words per minute.".format(wpm)
    }

    private fun clamp(value: Int): Int = value.coerceIn(0, 100)
}
