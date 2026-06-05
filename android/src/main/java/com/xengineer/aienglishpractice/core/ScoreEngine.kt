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
                reason = "发现 $issueCount 处语法或表达问题。"
            ),
            fluency = ScoreDetail(
                score = scoreFluency(wordCount, durationMs),
                reason = fluencyReason(wordCount, durationMs)
            ),
            pronunciation = ScoreDetail(
                score = clamp(((asrConfidence ?: 0f) * 100).toInt()),
                reason = if (asrConfidence == null) {
                    "暂无语音识别置信度。"
                } else {
                    "语音识别置信度为 %.2f。".format(asrConfidence)
                }
            ),
            completion = ScoreDetail(
                score = if (totalGoals <= 0) 0 else clamp((matchedGoals * 100f / totalGoals).toInt()),
                reason = "已命中 $matchedGoals / $totalGoals 个场景目标。"
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
        if (wordCount <= 0 || durationMs <= 0) return "暂无有效的语音时长。"
        val wpm = wordCount / (durationMs / 60000f)
        return "语速约为每分钟 %.0f 个单词。".format(wpm)
    }

    private fun clamp(value: Int): Int = value.coerceIn(0, 100)
}
