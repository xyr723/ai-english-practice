package com.xengineer.aienglishpractice.network

import com.xengineer.aienglishpractice.core.CoachFeedbackSource
import com.xengineer.aienglishpractice.core.PracticeSummary
import com.xengineer.aienglishpractice.core.ScoreBundle
import com.xengineer.aienglishpractice.core.ScoreDetail
import com.xengineer.aienglishpractice.core.SummaryScoreBreakdown
import com.xengineer.aienglishpractice.core.SummaryTurnReview
import com.xengineer.aienglishpractice.core.TurnResult
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class SummaryApiClient(
    private val baseUrl: String,
    private val timeoutMs: Int = 20000
) {
    suspend fun summarize(payload: SummaryPayload): PracticeSummary = withContext(Dispatchers.IO) {
        val connection = (URL("${baseUrl.trimEnd('/')}/summary").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { output ->
                output.write(payload.toJson().toString().toByteArray(Charsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val responseText = if (statusCode in 200..299) {
                connection.inputStream.bufferedReader().use { reader -> reader.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { reader -> reader.readText() }.orEmpty()
            }

            if (statusCode !in 200..299) {
                throw IOException("HTTP $statusCode ${responseText.ifBlank { "来自云端" }}")
            }

            JSONObject(responseText).toPracticeSummary()
        } finally {
            connection.disconnect()
        }
    }
}

data class SummaryPayload(
    val scenarioId: String,
    val turns: List<TurnResult>
) {
    fun toJson(): JSONObject = JSONObject()
        .put("scenarioId", scenarioId)
        .put(
            "turns",
            JSONArray().apply {
                turns.forEach { turn -> put(turn.toJson()) }
            }
        )
}

private fun TurnResult.toJson(): JSONObject = JSONObject()
    .put("userText", userText)
    .put("betterExpression", betterExpression)
    .put("reply", reply)
    .put("replyTranslation", replyTranslation)
    .put("scores", scores.scoreJson())
    .put("tips", tips.toJsonArray())

private fun ScoreBundle.scoreJson(): JSONObject = JSONObject()
    .put("grammar", grammar.toJson())
    .put("fluency", fluency.toJson())
    .put("pronunciation", pronunciation.toJson())
    .put("completion", completion.toJson())

private fun ScoreDetail.toJson(): JSONObject = JSONObject()
    .put("score", score)
    .put("reason", reason)

private fun JSONObject.toPracticeSummary(): PracticeSummary {
    val turnReviews = optJSONArray("turnReviews").toTurnReviews()
    return PracticeSummary(
        turnCount = optInt("turnCount", turnReviews.size),
        averageScore = optInt("averageScore", 0),
        strengths = stringList("strengths"),
        improvements = stringList("improvements"),
        nextGoal = optString("nextGoal"),
        scoreBreakdown = optJSONArray("scoreBreakdown").toScoreBreakdown(),
        turnReviews = turnReviews,
        practicePlan = stringList("practicePlan"),
        source = CoachFeedbackSource.fromBackendSource(optString("source"))
    )
}

private fun JSONArray?.toScoreBreakdown(): List<SummaryScoreBreakdown> {
    if (this == null) return emptyList()
    return List(length()) { index ->
        val item = getJSONObject(index)
        SummaryScoreBreakdown(
            label = item.optString("label"),
            score = item.optInt("score", 0),
            reason = item.optString("reason")
        )
    }
}

private fun JSONArray?.toTurnReviews(): List<SummaryTurnReview> {
    if (this == null) return emptyList()
    return List(length()) { index ->
        val item = getJSONObject(index)
        SummaryTurnReview(
            index = item.optInt("index", index + 1),
            userText = item.optString("userText"),
            betterExpression = item.optString("betterExpression"),
            reply = item.optString("reply"),
            score = item.optInt("score", 0),
            tips = item.optJSONArray("tips").toStringList()
        )
    }
}

private fun JSONObject.stringList(key: String): List<String> =
    optJSONArray(key).toStringList()

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return List(length()) { index -> optString(index) }.filter { it.isNotBlank() }
}

private fun List<String>.toJsonArray(): JSONArray = JSONArray().apply {
    forEach { item -> put(item) }
}
