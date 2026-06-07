package com.xengineer.aienglishpractice.core

class ScoreEngine {
    fun score(
        text: String,
        durationMs: Int,
        asrConfidence: Float?,
        correction: CorrectionResult,
        matchedGoals: Int,
        totalGoals: Int,
        recognitionAlternatives: List<RecognitionAlternative> = emptyList()
    ): ScoreBundle {
        val wordCount = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val issueCount = correction.issues.count { issue -> issue.type != "scenario" }
        val pronunciationAssessment = assessPronunciation(
            text = text,
            wordCount = wordCount,
            durationMs = durationMs,
            asrConfidence = asrConfidence,
            alternatives = recognitionAlternatives
        )

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
                score = pronunciationAssessment.score,
                reason = pronunciationAssessment.reason
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

    private data class PronunciationAssessment(
        val score: Int,
        val reason: String
    )

    private fun assessPronunciation(
        text: String,
        wordCount: Int,
        durationMs: Int,
        asrConfidence: Float?,
        alternatives: List<RecognitionAlternative>
    ): PronunciationAssessment {
        if (asrConfidence == null) {
            return PronunciationAssessment(0, "暂无语音识别置信度，无法评估发音清晰度。")
        }

        val baseScore = clamp((asrConfidence * 100).toInt())
        val stability = candidateStability(text, alternatives)
        val stabilityPenalty = ((100 - stability) * 0.32f).toInt()
        val tempoPenalty = pronunciationTempoPenalty(wordCount, durationMs)
        val score = clamp(baseScore - stabilityPenalty - tempoPenalty)
        val uncertainWords = uncertainWords(text, alternatives)
        val uncertainPart = if (uncertainWords.isEmpty()) {
            "候选句稳定。"
        } else {
            "建议重点重读：${uncertainWords.joinToString("、")}。"
        }
        val tempoPart = pronunciationTempoReason(wordCount, durationMs)

        return PronunciationAssessment(
            score = score,
            reason = "语音识别置信度为 %.2f，候选句稳定度为 $stability 分。$uncertainPart $tempoPart".format(asrConfidence)
        )
    }

    private fun candidateStability(text: String, alternatives: List<RecognitionAlternative>): Int {
        val primaryTokens = contentTokens(text)
        if (primaryTokens.isEmpty() || alternatives.size <= 1) return 100

        val stabilityScores = alternatives
            .drop(1)
            .map { alternative ->
                val alternativeTokens = contentTokens(alternative.transcript).toSet()
                if (alternativeTokens.isEmpty()) {
                    0
                } else {
                    (primaryTokens.count { token -> token in alternativeTokens } * 100f / primaryTokens.size).toInt()
                }
            }

        return stabilityScores.average().toInt().coerceIn(0, 100)
    }

    private fun uncertainWords(text: String, alternatives: List<RecognitionAlternative>): List<String> {
        val primaryTokens = contentTokens(text)
            .filter { token -> token.length > 3 }
            .distinct()
        if (primaryTokens.isEmpty() || alternatives.size <= 1) return emptyList()

        val alternativeTokenSets = alternatives.drop(1).map { alternative ->
            contentTokens(alternative.transcript).toSet()
        }

        return primaryTokens
            .filter { token -> alternativeTokenSets.any { tokens -> token !in tokens } }
            .take(3)
    }

    private fun pronunciationTempoPenalty(wordCount: Int, durationMs: Int): Int {
        if (wordCount <= 0 || durationMs <= 0) return 0
        val wpm = wordCount / (durationMs / 60000f)
        return when {
            wpm < 50f -> 6
            wpm > 170f -> 6
            else -> 0
        }
    }

    private fun pronunciationTempoReason(wordCount: Int, durationMs: Int): String {
        if (wordCount <= 0 || durationMs <= 0) return "暂无有效语速信息。"
        val wpm = wordCount / (durationMs / 60000f)
        return if (wpm < 50f || wpm > 170f) {
            "语速约为每分钟 %.0f 个词，建议放慢并逐词说清。".format(wpm)
        } else {
            "语速节奏适合发音判断。"
        }
    }

    private fun contentTokens(text: String): List<String> =
        Regex("[a-zA-Z']+")
            .findAll(text.lowercase())
            .map { match -> match.value.trim('\'') }
            .filter { token -> token.isNotBlank() }
            .toList()
}
