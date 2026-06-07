package com.xengineer.aienglishpractice.ui.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xengineer.aienglishpractice.core.CoachBackendUiState
import com.xengineer.aienglishpractice.core.CoachCharacterState
import com.xengineer.aienglishpractice.core.CoachFeedbackSource
import com.xengineer.aienglishpractice.core.PracticeState
import com.xengineer.aienglishpractice.core.PracticeStep
import com.xengineer.aienglishpractice.core.PracticeUiState
import com.xengineer.aienglishpractice.core.PracticeHistoryEntry
import com.xengineer.aienglishpractice.core.PracticeSession
import com.xengineer.aienglishpractice.core.PracticeScenario
import com.xengineer.aienglishpractice.core.PracticeSummary
import com.xengineer.aienglishpractice.core.RuleCorrectionEngine
import com.xengineer.aienglishpractice.core.ScenarioCatalog
import com.xengineer.aienglishpractice.core.ScoreEngine
import com.xengineer.aienglishpractice.core.SpeechListenMode
import com.xengineer.aienglishpractice.core.SummaryTurnReview
import com.xengineer.aienglishpractice.core.VoiceInputMode
import com.xengineer.aienglishpractice.core.VoiceUiState
import com.xengineer.aienglishpractice.network.CoachAnalyzePayload
import com.xengineer.aienglishpractice.network.CoachApiClient
import com.xengineer.aienglishpractice.ui.shared.DarkPanel
import com.xengineer.aienglishpractice.ui.shared.LightPanel
import com.xengineer.aienglishpractice.ui.shared.PrimaryAction
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors
import com.xengineer.aienglishpractice.voice.SpeechRecognitionCallbacks
import com.xengineer.aienglishpractice.voice.SpeechRecognizerAdapter
import com.xengineer.aienglishpractice.voice.TextToSpeechAdapter
import kotlinx.coroutines.launch

@Composable
fun PracticeScreen(
    scenarioId: String,
    coachBaseUrl: String = CoachBackendUiState.DEFAULT_BASE_URL,
    onBackHome: () -> Unit,
    onSessionFinished: (PracticeHistoryEntry) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scenario = remember(scenarioId) {
        ScenarioCatalog.findById(scenarioId) ?: ScenarioCatalog.recommended()
    }
    var session by remember(scenarioId) { mutableStateOf(newPracticeSession(scenario)) }
    var uiState by remember(scenarioId) { mutableStateOf(PracticeUiState.initial(scenario)) }
    var backendState by remember(scenarioId, coachBaseUrl) {
        mutableStateOf(CoachBackendUiState.initial(coachBaseUrl))
    }
    val coachApiClient = remember(backendState.baseUrl) { CoachApiClient(backendState.baseUrl) }
    val demoTranscript = remember(scenarioId) { demoTranscriptFor(scenario.id) }
    val speechRecognizer = remember(context) {
        SpeechRecognizerAdapter(context.applicationContext)
    }
    var hasAudioPermission by remember {
        mutableStateOf(context.hasRecordAudioPermission())
    }
    var voiceState by remember(scenarioId) {
        mutableStateOf(
            VoiceUiState.initial(
                recognizerAvailable = speechRecognizer.isAvailable(),
                audioPermissionGranted = hasAudioPermission
            )
        )
    }
    var ttsReady by remember { mutableStateOf(false) }
    val textToSpeech = remember(context) {
        TextToSpeechAdapter(
            context = context.applicationContext,
            onReadyChanged = { ready ->
                ttsReady = ready
            },
            onPlaybackChanged = { speaking ->
                coroutineScope.launch {
                    voiceState = voiceState.setTtsSpeaking(speaking)
                }
            }
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        val refreshedState = voiceState.withCapabilities(
            recognizerAvailable = speechRecognizer.isAvailable(),
            audioPermissionGranted = granted
        )
        voiceState = if (granted) refreshedState.useSpeechMode() else refreshedState
    }

    DisposableEffect(speechRecognizer, textToSpeech) {
        onDispose {
            speechRecognizer.destroy()
            textToSpeech.shutdown()
        }
    }

    LaunchedEffect(ttsReady) {
        voiceState = voiceState.setTtsReady(ttsReady)
    }

    LaunchedEffect(hasAudioPermission) {
        voiceState = voiceState.withCapabilities(
            recognizerAvailable = speechRecognizer.isAvailable(),
            audioPermissionGranted = hasAudioPermission
        )
    }

    fun restartScene() {
        session = newPracticeSession(scenario)
        uiState = PracticeUiState.initial(scenario)
        voiceState = voiceState.useDemoMode()
        backendState = backendState.useAuto()
    }

    fun submitLocalFeedback(transcript: String) {
        val result = session.submitTurn(
            text = transcript,
            durationMs = demoDurationFor(scenario.id),
            asrConfidence = demoConfidenceFor(scenario.id)
        )
        uiState = PracticeUiState.speaking(
            scenario = scenario,
            turnResult = result
        )
        if (voiceState.ttsEnabled) {
            textToSpeech.speak(result.reply)
        }
    }

    fun showFeedback() {
        val transcript = uiState.transcript.ifBlank { demoTranscript }
        val durationMs = demoDurationFor(scenario.id)
        val asrConfidence = demoConfidenceFor(scenario.id)

        if (!backendState.shouldTryBackend) {
            backendState = backendState.useLocalOnly()
            submitLocalFeedback(transcript)
            return
        }

        val activeBackendState = backendState.withChecking()
        backendState = activeBackendState
        coroutineScope.launch {
            try {
                val backendResult = coachApiClient.analyze(
                    CoachAnalyzePayload(
                        scenarioId = scenario.id,
                        turnText = transcript,
                        durationMs = durationMs,
                        asrConfidence = asrConfidence,
                        turnIndex = session.turnCount
                    )
                )
                session.recordAnalyzedTurn(backendResult)
                backendState = activeBackendState.withBackendSuccess(backendResult.source)
                uiState = PracticeUiState.speaking(
                    scenario = scenario,
                    turnResult = backendResult
                )
                if (voiceState.ttsEnabled) {
                    textToSpeech.speak(backendResult.reply)
                }
            } catch (error: Exception) {
                val failedState = activeBackendState.withBackendFailure(
                    error.message ?: "云端请求失败"
                )
                backendState = failedState
                if (failedState.shouldUseLocalFallback) {
                    submitLocalFeedback(transcript)
                } else {
                    uiState = PracticeUiState.error(
                        scenario = scenario,
                        message = failedState.statusText
                    )
                }
            }
        }
    }

    fun advancePrimary() {
        uiState = when (uiState.phase) {
            PracticeState.Idle -> PracticeUiState.listening(scenario)
            PracticeState.Listening -> PracticeUiState.recognizing(
                scenario = scenario,
                transcript = demoTranscript
            )

            PracticeState.Recognizing -> PracticeUiState.thinking(
                scenario = scenario,
                transcript = uiState.transcript
            )

            PracticeState.Thinking -> {
                showFeedback()
                return
            }

            PracticeState.Speaking -> PracticeUiState.listening(scenario)
            PracticeState.Finished -> {
                restartScene()
                return
            }

            PracticeState.Error -> PracticeUiState.initial(scenario)
        }
    }

    fun startSpeechInput(listenMode: SpeechListenMode = SpeechListenMode.Standard) {
        if (!speechRecognizer.isAvailable()) {
            voiceState = voiceState.withRecognitionError("当前设备不可用语音识别。")
            return
        }
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        voiceState = voiceState.startSpeechFromCurrentMode(listenMode)
        uiState = PracticeUiState.listening(scenario)
        speechRecognizer.startListening(
            SpeechRecognitionCallbacks(
                onReady = {
                    voiceState = voiceState.useSpeechMode().startListening(listenMode)
                    uiState = PracticeUiState.listening(scenario)
                },
                onPartial = { text ->
                    if (text.isNotBlank()) {
                        voiceState = voiceState.withPartialTranscript(text)
                        uiState = PracticeUiState.recognizing(
                            scenario = scenario,
                            transcript = text
                        )
                    }
                },
                onFinal = { text ->
                    val transcript = text.ifBlank { voiceState.bestTranscript }
                    voiceState = voiceState.withFinalTranscript(transcript)
                    if (transcript.isNotBlank()) {
                        uiState = PracticeUiState.recognizing(
                            scenario = scenario,
                            transcript = transcript
                        )
                    }
                },
                onError = { message ->
                    voiceState = voiceState.withRecognitionError(message)
                    uiState = PracticeUiState.error(
                        scenario = scenario,
                        message = "$message 可继续使用演示模式。"
                    )
                }
            ),
            listenMode = listenMode
        )
    }

    fun toggleVoiceMode() {
        voiceState = if (voiceState.mode == VoiceInputMode.SpeechRecognizer) {
            voiceState.useDemoMode()
        } else {
            if (!hasAudioPermission) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                voiceState
            } else {
                voiceState.useSpeechMode()
            }
        }
    }

    fun speakCoachText() {
        if (!voiceState.ttsEnabled) return
        textToSpeech.speak(replyText(scenario.opening, uiState))
    }

    val finishedSummary = uiState.summary

    StageScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            PracticeHeader(
                title = scenario.name,
                subtitle = "${scenario.level} · ${scenario.estimatedMinutes} 分钟 · ${scenario.sceneTone}",
                onBackHome = onBackHome
            )
            if (uiState.phase == PracticeState.Finished && finishedSummary != null) {
                EnhancedSummaryPage(
                    scenario = scenario,
                    summary = finishedSummary,
                    onPracticeAgain = { restartScene() },
                    onBackHome = onBackHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusPanel(
                        uiState = uiState,
                        voiceState = voiceState,
                        backendState = backendState,
                        modifier = Modifier.weight(0.8f)
                    )
                    FeedbackPanel(uiState = uiState, modifier = Modifier.weight(1f))
                    CoachPanel(
                        opening = scenario.opening,
                        openingTranslation = scenario.openingTranslation,
                        characterState = CoachCharacterState.from(
                            practiceState = uiState.phase,
                            isTtsSpeaking = voiceState.isTtsSpeaking
                        ),
                        uiState = uiState,
                        voiceState = voiceState,
                        onSpeakCoach = { speakCoachText() },
                        modifier = Modifier.weight(1.1f)
                    )
                }
                PracticeControls(
                    uiState = uiState,
                    voiceState = voiceState,
                    backendState = backendState,
                    onPrimaryAction = { advancePrimary() },
                    onStartSpeech = { startSpeechInput() },
                    onStartLongSpeech = { startSpeechInput(SpeechListenMode.Extended) },
                    onToggleVoiceMode = { toggleVoiceMode() },
                    onToggleTts = { voiceState = voiceState.setTtsEnabled(!voiceState.ttsEnabled) },
                    onFinish = {
                        val summary = session.finish()
                        val entry = PracticeHistoryEntry.fromSummary(
                            scenario = scenario,
                            summary = summary
                        )
                        onSessionFinished(entry)
                        uiState = PracticeUiState.finished(
                            scenario = scenario,
                            summary = summary
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PracticeHeader(
    title: String,
    subtitle: String,
    onBackHome: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = subtitle, color = Color(0xFFD9E7EA))
        }
        TextButton(onClick = onBackHome) {
            Text("首页", color = Color.White)
        }
    }
}

@Composable
private fun StatusPanel(
    uiState: PracticeUiState,
    voiceState: VoiceUiState,
    backendState: CoachBackendUiState,
    modifier: Modifier = Modifier
) {
    DarkPanel(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("实时状态", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(
                text = uiState.statusTitle,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = uiState.statusBody,
                color = Color(0xFFEAD7C4),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "语音：${voiceState.statusText}",
                color = Color(0xFFEAD7C4),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "朗读：${if (voiceState.ttsReady) "就绪" else "初始化"} · ${if (voiceState.ttsEnabled) "开启" else "静音"}",
                color = Color(0xFFEAD7C4),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "教练：${backendState.statusText}",
                color = Color(0xFFEAD7C4),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            uiState.timeline.forEach { step ->
                TimelineRow(step)
            }
        }
    }
}

@Composable
private fun TimelineRow(step: PracticeStep) {
    val marker = when {
        step.isActive -> ">"
        step.isComplete -> "✓"
        else -> "-"
    }
    val markerColor = when {
        step.isActive -> PracticeColors.Amber
        step.isComplete -> PracticeColors.Mint
        else -> Color(0xFFBFA896)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = marker,
            modifier = Modifier.width(44.dp),
            color = markerColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = step.label,
            color = if (step.isActive) Color.White else Color(0xFFEAD7C4),
            fontWeight = if (step.isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FeedbackPanel(uiState: PracticeUiState, modifier: Modifier = Modifier) {
    val turnResult = uiState.turnResult
    val transcript = when {
        uiState.phase == PracticeState.Listening -> "正在等待你的回答..."
        uiState.transcript.isNotBlank() -> uiState.transcript
        else -> "识别后会显示转写文本。"
    }

    DarkPanel(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("你说了", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(transcript, color = Color.White)
            Text("更自然的表达", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(
                turnResult?.betterExpression ?: "教练分析后会显示优化表达。",
                color = Color.White
            )
            Text("提示", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(turnResult?.tips?.joinToString("\n") ?: "暂无提示。", color = Color.White)
        }
    }
}

@Composable
private fun CoachPanel(
    opening: String,
    openingTranslation: String,
    characterState: CoachCharacterState,
    uiState: PracticeUiState,
    voiceState: VoiceUiState,
    onSpeakCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    val turnResult = uiState.turnResult
    val summary = uiState.summary

    LightPanel(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CoachCharacterPanel(state = characterState)
            Text("教练回复", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
            Text(replyText(opening, uiState))
            val translation = replyTranslationText(openingTranslation, uiState)
            if (translation.isNotBlank()) {
                Text(translation, color = PracticeColors.Ink)
            }
            Text(
                text = "来源：${turnResult?.source?.label() ?: "待生成"}",
                color = PracticeColors.Ink
            )
            ScoreRow("语法", turnResult?.scores?.grammar?.score)
            ScoreRow("流利度", turnResult?.scores?.fluency?.score)
            ScoreRow("发音", turnResult?.scores?.pronunciation?.score)
            ScoreRow("完成度", turnResult?.scores?.completion?.score)
            if (summary != null) {
                Text("课后总结", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
                Text("轮次：${summary.turnCount}  平均分：${summary.averageScore}")
                Text(summary.nextGoal)
            }
            if (voiceState.ttsEnabled) {
                PrimaryAction(
                    text = if (voiceState.ttsReady) "朗读回复" else "朗读加载中",
                    onClick = onSpeakCoach
                )
            }
        }
    }
}

@Composable
private fun EnhancedSummaryPage(
    scenario: PracticeScenario,
    summary: PracticeSummary,
    onPracticeAgain: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DarkPanel(modifier = Modifier.weight(0.95f).fillMaxHeight()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("课后总结", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
                Text(
                    text = "${summary.averageScore}",
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${scenario.name} · ${summary.turnCount} 轮完成",
                    color = Color(0xFFEAD7C4),
                    style = MaterialTheme.typography.titleMedium
                )
                Text("下一目标", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
                Text(summary.nextGoal, color = Color.White)
                Text("复练计划", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
                summary.practicePlan.forEachIndexed { index, item ->
                    Text("${index + 1}. $item", color = Color(0xFFEAD7C4))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryAction(text = "再练一次", onClick = onPracticeAgain)
                    PrimaryAction(text = "回首页", onClick = onBackHome)
                }
            }
        }
        LightPanel(modifier = Modifier.weight(1.1f).fillMaxHeight()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("能力雷达", color = PracticeColors.Cafe, fontWeight = FontWeight.Bold)
                summary.scoreBreakdown.forEach { score ->
                    SummaryScoreRow(score.label, score.score, score.reason)
                }
                Text("优势", color = PracticeColors.Cafe, fontWeight = FontWeight.Bold)
                summary.strengths.forEach { item -> Text("• $item", color = PracticeColors.Ink) }
                Text("需要改进", color = PracticeColors.Cafe, fontWeight = FontWeight.Bold)
                summary.improvements.forEach { item -> Text("• $item", color = PracticeColors.Ink) }
            }
        }
        LightPanel(modifier = Modifier.weight(1.25f).fillMaxHeight()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("逐轮复盘", color = PracticeColors.Cafe, fontWeight = FontWeight.Bold)
                summary.turnReviews.forEach { review ->
                    SummaryTurnReviewCard(review)
                }
            }
        }
    }
}

@Composable
private fun SummaryScoreRow(label: String, score: Int, reason: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
            Text("$score", color = PracticeColors.Cafe, fontWeight = FontWeight.Bold)
        }
        Text(reason, color = PracticeColors.Ink, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SummaryTurnReviewCard(
    review: SummaryTurnReview
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "第 ${review.index} 轮 · ${review.score} 分",
            color = PracticeColors.Cafe,
            fontWeight = FontWeight.Bold
        )
        Text("你说：${review.userText}", color = PracticeColors.Ink)
        Text("优化：${review.betterExpression}", color = PracticeColors.Ink)
        if (review.tips.isNotEmpty()) {
            Text("提示：${review.tips.joinToString("；")}", color = PracticeColors.Ink)
        }
    }
}

@Composable
private fun ScoreRow(label: String, score: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(76.dp), color = PracticeColors.Ink)
        Text((score ?: "--").toString(), fontWeight = FontWeight.Bold, color = PracticeColors.Ink)
    }
}

@Composable
private fun PracticeControls(
    uiState: PracticeUiState,
    voiceState: VoiceUiState,
    backendState: CoachBackendUiState,
    onPrimaryAction: () -> Unit,
    onStartSpeech: () -> Unit,
    onStartLongSpeech: () -> Unit,
    onToggleVoiceMode: () -> Unit,
    onToggleTts: () -> Unit,
    onFinish: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimaryAction(
                text = if (backendState.isChecking) "分析中..." else uiState.primaryAction,
                onClick = onPrimaryAction,
                modifier = Modifier.widthIn(min = 116.dp)
            )
            Spacer(Modifier.width(8.dp))
            PrimaryAction(
                text = voiceState.speechAction,
                onClick = onStartSpeech,
                modifier = Modifier.widthIn(min = 112.dp),
                onLongClick = onStartLongSpeech
            )
            if (uiState.canFinish) {
                Spacer(Modifier.width(8.dp))
                PrimaryAction(text = "完成", onClick = onFinish)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onToggleVoiceMode) {
                Text(voiceState.modeAction, color = Color.White)
            }
            TextButton(onClick = onToggleTts) {
                Text(voiceState.ttsAction, color = Color.White)
            }
        }
    }
}

private fun newPracticeSession(scenario: PracticeScenario): PracticeSession = PracticeSession(
    scenario = scenario,
    correctionEngine = RuleCorrectionEngine(),
    scoreEngine = ScoreEngine()
).also { session -> session.start() }

private fun replyText(opening: String, uiState: PracticeUiState): String = when (uiState.phase) {
    PracticeState.Idle,
    PracticeState.Listening,
    PracticeState.Recognizing -> opening

    PracticeState.Thinking -> "请稍等，教练正在分析你的回答。"
    PracticeState.Speaking -> uiState.turnResult?.reply ?: opening
    PracticeState.Finished -> "本次练习已完成，请查看总结。"
    PracticeState.Error -> "恢复练习后，可继续当前场景。"
}

private fun replyTranslationText(openingTranslation: String, uiState: PracticeUiState): String = when (uiState.phase) {
    PracticeState.Idle,
    PracticeState.Listening,
    PracticeState.Recognizing -> openingTranslation

    PracticeState.Speaking -> uiState.turnResult?.replyTranslation.orEmpty()
    else -> ""
}

private fun demoTranscriptFor(scenarioId: String): String = when (scenarioId) {
    "interview" -> "I have experience in a team project, and I built a small Android demo."
    "meeting" -> "I think the timeline risk is high, and the next step should be a shorter plan."
    else -> "I want order a coffee"
}

private fun demoDurationFor(scenarioId: String): Int = when (scenarioId) {
    "interview" -> 8500
    "meeting" -> 9000
    else -> 6000
}

private fun demoConfidenceFor(scenarioId: String): Float = when (scenarioId) {
    "interview" -> 0.86f
    "meeting" -> 0.9f
    else -> 0.8f
}

private fun android.content.Context.hasRecordAudioPermission(): Boolean =
    checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

private fun CoachFeedbackSource.label(): String = when (this) {
    CoachFeedbackSource.BackendApi -> "云端教练"
    CoachFeedbackSource.BackendRule -> "云端规则"
    CoachFeedbackSource.LanguageTool -> "LanguageTool"
    CoachFeedbackSource.BackendRuleFallback -> "云端规则 fallback"
    CoachFeedbackSource.LocalFallback -> "本地分析"
    CoachFeedbackSource.BackendError -> "云端错误"
}
