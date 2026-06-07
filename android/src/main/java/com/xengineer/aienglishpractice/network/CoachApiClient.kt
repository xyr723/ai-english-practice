package com.xengineer.aienglishpractice.network

import com.xengineer.aienglishpractice.core.CoachFeedbackSource
import com.xengineer.aienglishpractice.core.ScoreBundle
import com.xengineer.aienglishpractice.core.ScoreDetail
import com.xengineer.aienglishpractice.core.TurnResult
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CoachApiClient(
    private val baseUrl: String,
    private val timeoutMs: Int = 8000
) {
    suspend fun analyze(request: CoachAnalyzePayload): TurnResult = withContext(Dispatchers.IO) {
        val connection = (URL("${baseUrl.trimEnd('/')}/coach/analyze").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { output ->
                output.write(request.toJson().toString().toByteArray(Charsets.UTF_8))
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

            JSONObject(responseText).toTurnResult(request.turnText)
        } finally {
            connection.disconnect()
        }
    }
}

data class CoachAnalyzePayload(
    val scenarioId: String,
    val turnText: String,
    val durationMs: Int,
    val asrConfidence: Float?,
    val turnIndex: Int
) {
    fun toJson(): JSONObject = JSONObject()
        .put("scenarioId", scenarioId)
        .put("turnText", turnText)
        .put("history", org.json.JSONArray())
        .put("durationMs", durationMs)
        .put("asrConfidence", asrConfidence)
        .put("turnIndex", turnIndex)
}

private fun JSONObject.toTurnResult(userText: String): TurnResult = TurnResult(
    userText = userText,
    betterExpression = getString("betterExpression"),
    reply = getString("reply"),
    replyTranslation = optString("replyTranslation"),
    scores = getJSONObject("scores").toScoreBundle(),
    tips = getJSONArray("tips").let { tipsJson ->
        List(tipsJson.length()) { index -> tipsJson.getString(index) }
    },
    source = CoachFeedbackSource.fromBackendSource(optString("source"))
)

private fun JSONObject.toScoreBundle(): ScoreBundle = ScoreBundle(
    grammar = getJSONObject("grammar").toScoreDetail(),
    fluency = getJSONObject("fluency").toScoreDetail(),
    pronunciation = getJSONObject("pronunciation").toScoreDetail(),
    completion = getJSONObject("completion").toScoreDetail()
)

private fun JSONObject.toScoreDetail(): ScoreDetail = ScoreDetail(
    score = getInt("score"),
    reason = getString("reason")
)
