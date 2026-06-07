package com.xengineer.aienglishpractice.ui.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.xengineer.aienglishpractice.R
import com.xengineer.aienglishpractice.core.CoachBackendUiState
import com.xengineer.aienglishpractice.core.CoachCharacterState
import com.xengineer.aienglishpractice.core.CoachFeedbackSource
import com.xengineer.aienglishpractice.core.EngineSelectionConfig
import com.xengineer.aienglishpractice.core.PracticeState
import com.xengineer.aienglishpractice.core.PracticeUiState
import com.xengineer.aienglishpractice.core.PracticeHistoryEntry
import com.xengineer.aienglishpractice.core.PracticeSession
import com.xengineer.aienglishpractice.core.PracticeScenario
import com.xengineer.aienglishpractice.core.PracticeSummary
import com.xengineer.aienglishpractice.core.RuleCorrectionEngine
import com.xengineer.aienglishpractice.core.ScenarioCatalog
import com.xengineer.aienglishpractice.core.ScoreEngine
import com.xengineer.aienglishpractice.core.SceneDescriptor
import com.xengineer.aienglishpractice.core.SpeechListenMode
import com.xengineer.aienglishpractice.core.SummaryScoreBreakdown
import com.xengineer.aienglishpractice.core.SummaryTurnReview
import com.xengineer.aienglishpractice.core.TurnResult
import com.xengineer.aienglishpractice.core.VoiceInputMode
import com.xengineer.aienglishpractice.core.VoiceUiState
import com.xengineer.aienglishpractice.network.CoachAnalyzePayload
import com.xengineer.aienglishpractice.network.CoachApiClient
import com.xengineer.aienglishpractice.ui.shared.DarkPanel
import com.xengineer.aienglishpractice.ui.shared.AbilityRadarChart
import com.xengineer.aienglishpractice.ui.shared.GlassPanel
import com.xengineer.aienglishpractice.ui.shared.LightPanel
import com.xengineer.aienglishpractice.ui.shared.PrimaryAction
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors
import com.xengineer.aienglishpractice.voice.SpeechRecognitionCallbacks
import com.xengineer.aienglishpractice.voice.SpeechRecognizerAdapter
import com.xengineer.aienglishpractice.voice.TextToSpeechAdapter
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private data class ConversationMessage(
    val speaker: String,
    val text: String,
    val secondaryText: String? = null,
    val fromCoach: Boolean = false
)

@Composable
fun PracticeScreen(
    scenarioId: String,
    scenarioOverride: PracticeScenario? = null,
    coachBaseUrl: String = CoachBackendUiState.DEFAULT_BASE_URL,
    engineSelectionConfig: EngineSelectionConfig = EngineSelectionConfig.default(),
    onBackHome: () -> Unit,
    onSessionFinished: (PracticeHistoryEntry) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scenario = remember(scenarioId, scenarioOverride) {
        scenarioOverride ?: ScenarioCatalog.findById(scenarioId) ?: ScenarioCatalog.recommended()
    }
    var session by remember(scenarioId) { mutableStateOf(newPracticeSession(scenario)) }
    var uiState by remember(scenarioId) { mutableStateOf(PracticeUiState.initial(scenario)) }
    var practiceStartedAtMs by remember(scenarioId) { mutableStateOf(System.currentTimeMillis()) }
    var conversationMessages by remember(scenarioId) {
        mutableStateOf(initialConversationMessages(scenario))
    }
    var cloudErrorMessage by remember(scenarioId) { mutableStateOf<String?>(null) }
    var isMicPressed by remember(scenarioId) { mutableStateOf(false) }
    var backendState by remember(scenarioId, coachBaseUrl, engineSelectionConfig) {
        mutableStateOf(
            CoachBackendUiState.initial(
                baseUrl = coachBaseUrl,
                mode = engineSelectionConfig.preferredBackendMode
            )
        )
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
        practiceStartedAtMs = System.currentTimeMillis()
        conversationMessages = initialConversationMessages(scenario)
        cloudErrorMessage = null
        voiceState = voiceState.useDemoMode()
        backendState = CoachBackendUiState.initial(
            baseUrl = coachBaseUrl,
            mode = engineSelectionConfig.preferredBackendMode
        )
    }

    fun appendConversationTurn(transcript: String, result: TurnResult) {
        conversationMessages = conversationMessages + listOf(
            ConversationMessage(
                speaker = "You",
                text = transcript,
                secondaryText = "Better expression: ${result.betterExpression}"
            ),
            ConversationMessage(
                speaker = "Coach",
                text = result.reply,
                secondaryText = result.replyTranslation.takeIf { it.isNotBlank() },
                fromCoach = true
            )
        )
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
        appendConversationTurn(transcript, result)
        if (voiceState.ttsEnabled) {
            textToSpeech.speak(result.reply)
        }
    }

    fun showFeedback(transcriptOverride: String? = null) {
        val transcript = transcriptOverride?.takeIf { it.isNotBlank() }
            ?: uiState.transcript.ifBlank { demoTranscript }
        val durationMs = demoDurationFor(scenario.id)
        val asrConfidence = demoConfidenceFor(scenario.id)

        if (!backendState.shouldTryBackend) {
            backendState = backendState.useLocalOnly()
            submitLocalFeedback(transcript)
            return
        }

        cloudErrorMessage = null
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
                cloudErrorMessage = null
                uiState = PracticeUiState.speaking(
                    scenario = scenario,
                    turnResult = backendResult
                )
                appendConversationTurn(transcript, backendResult)
                if (voiceState.ttsEnabled) {
                    textToSpeech.speak(backendResult.reply)
                }
            } catch (error: Exception) {
                val failedState = activeBackendState.withBackendFailure(
                    error.message ?: "云端请求失败"
                )
                backendState = failedState
                cloudErrorMessage = if (failedState.shouldUseLocalFallback) {
                    "云端暂时不可用，已使用本地分析完成本轮。"
                } else {
                    "云端暂时不可用，请检查后端服务和设备连接。"
                }
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

    fun submitRecognizedSpeech(transcript: String) {
        if (transcript.isBlank()) return
        uiState = PracticeUiState.thinking(
            scenario = scenario,
            transcript = transcript
        )
        showFeedback(transcript)
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
                        submitRecognizedSpeech(transcript)
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
        if (uiState.phase == PracticeState.Finished && finishedSummary != null) {
            EnhancedSummaryPage(
                scenario = scenario,
                summary = finishedSummary,
                onPracticeAgain = { restartScene() },
                onBackHome = onBackHome,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            ImmersivePracticeStage(
                scenario = scenario,
                uiState = uiState,
                voiceState = voiceState,
                backendState = backendState,
                conversationMessages = conversationMessages,
                isMicPressed = isMicPressed,
                characterState = CoachCharacterState.from(
                    practiceState = uiState.phase,
                    isTtsSpeaking = voiceState.isTtsSpeaking
                ),
                onBackHome = onBackHome,
                onPrimaryAction = { advancePrimary() },
                onStartSpeech = { startSpeechInput() },
                onStartLongSpeech = { startSpeechInput(SpeechListenMode.Extended) },
                onMicPressChanged = { pressed -> isMicPressed = pressed },
                onToggleVoiceMode = { toggleVoiceMode() },
                onToggleTts = { voiceState = voiceState.setTtsEnabled(!voiceState.ttsEnabled) },
                onSpeakCoach = { speakCoachText() },
                onFinish = {
                    val summary = session.finish()
                    val entry = PracticeHistoryEntry.fromSummary(
                        scenario = scenario,
                        summary = summary,
                        durationLabel = durationLabelFor(System.currentTimeMillis() - practiceStartedAtMs)
                    )
                    onSessionFinished(entry)
                    uiState = PracticeUiState.finished(
                        scenario = scenario,
                        summary = summary
                    )
                }
            )
        }

        cloudErrorMessage?.let { message ->
            PracticeCloudErrorDialog(
                message = message,
                onDismiss = { cloudErrorMessage = null }
            )
        }
    }
}

@Composable
private fun ImmersivePracticeStage(
    scenario: PracticeScenario,
    uiState: PracticeUiState,
    voiceState: VoiceUiState,
    backendState: CoachBackendUiState,
    conversationMessages: List<ConversationMessage>,
    isMicPressed: Boolean,
    characterState: CoachCharacterState,
    onBackHome: () -> Unit,
    onPrimaryAction: () -> Unit,
    onStartSpeech: () -> Unit,
    onStartLongSpeech: () -> Unit,
    onMicPressChanged: (Boolean) -> Unit,
    onToggleVoiceMode: () -> Unit,
    onToggleTts: () -> Unit,
    onSpeakCoach: () -> Unit,
    onFinish: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
    ) {
        val compact = maxWidth < 780.dp
        val edge = if (compact) 12.dp else 18.dp
        val leftCardWidth = if (compact) 214.dp else 252.dp
        val bubbleWidth = if (compact) 210.dp else 248.dp
        val avatarSize = if (compact) 190.dp else 260.dp
        val dockHeight = if (compact) 76.dp else 86.dp

        SceneBackgroundImage(
            descriptor = scenario.sceneDescriptor,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
        )

        TopStageBar(
            scenario = scenario,
            voiceState = voiceState,
            onBackHome = onBackHome,
            onToggleVoiceMode = onToggleVoiceMode,
            onToggleTts = onToggleTts,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = edge, vertical = 10.dp)
        )

        ConversationHistoryCard(
            messages = conversationMessagesFor(
                baseMessages = conversationMessages,
                uiState = uiState,
                isMicPressed = isMicPressed
            ),
            isListening = voiceState.isListening || isMicPressed,
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = edge, top = if (compact) 58.dp else 68.dp, bottom = dockHeight + 16.dp)
                .width(leftCardWidth)
        )

        CoachBubble(
            text = replyText(scenario.opening, uiState),
            translation = replyTranslationText(scenario.openingTranslation, uiState),
            onSpeak = onSpeakCoach,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(
                    end = avatarSize - if (compact) 18.dp else 24.dp,
                    bottom = if (compact) 34.dp else 62.dp
                )
                .width(bubbleWidth)
        )

        CoachCharacterPanel(
            state = characterState,
            showLabel = false,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = if (compact) 8.dp else 20.dp, bottom = dockHeight - 4.dp)
                .size(avatarSize)
        )

        ScoreStrip(
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = dockHeight + 10.dp)
        )

        BottomPracticeDock(
            uiState = uiState,
            voiceState = voiceState,
            backendState = backendState,
            isMicPressed = isMicPressed,
            onPrimaryAction = onPrimaryAction,
            onStartSpeech = onStartSpeech,
            onStartLongSpeech = onStartLongSpeech,
            onMicPressChanged = onMicPressChanged,
            onFinish = onFinish,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(dockHeight)
        )
    }
}

@Composable
private fun SceneBackgroundImage(
    descriptor: SceneDescriptor = SceneDescriptor.restaurant(),
    modifier: Modifier = Modifier
) {
    backgroundDrawableFor(descriptor)?.let { drawable ->
        Image(
            painter = painterResource(drawable),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }

    GeneratedSceneBackground(
        descriptor = descriptor,
        modifier = modifier
    )
}

private fun backgroundDrawableFor(descriptor: SceneDescriptor): Int? = when (descriptor.visualTheme) {
    "restaurant" -> R.drawable.bg_restaurant_photo
    "airport" -> R.drawable.bg_airport_photo
    "office" -> R.drawable.bg_office_photo
    "meeting" -> R.drawable.bg_meeting_photo
    "shopping" -> R.drawable.bg_shopping_photo
    else -> null
}

@Composable
private fun GeneratedSceneBackground(
    descriptor: SceneDescriptor,
    modifier: Modifier = Modifier
) {
    val baseColor = parseSceneColor(descriptor.backgroundColor)

    Canvas(
        modifier = modifier.background(baseColor)
    ) {
        val w = size.width
        val h = size.height

        drawRect(Color.Black.copy(alpha = 0.04f))
        drawRoundRect(
            color = Color.White.copy(alpha = 0.28f),
            topLeft = Offset(w * 0.58f, h * 0.12f),
            size = Size(w * 0.3f, h * 0.36f)
        )
        drawLine(Color.White.copy(alpha = 0.42f), Offset(w * 0.72f, h * 0.12f), Offset(w * 0.72f, h * 0.48f), 3f)
        drawLine(Color.White.copy(alpha = 0.42f), Offset(w * 0.58f, h * 0.3f), Offset(w * 0.88f, h * 0.3f), 3f)

        drawRoundRect(
            color = Color(0xFF7A5A43).copy(alpha = 0.82f),
            topLeft = Offset(w * 0.08f, h * 0.62f),
            size = Size(w * 0.56f, h * 0.2f)
        )
        drawRoundRect(
            color = Color(0xFF3D2F2B).copy(alpha = 0.72f),
            topLeft = Offset(w * 0.08f, h * 0.58f),
            size = Size(w * 0.56f, h * 0.08f)
        )

        descriptor.objects.forEachIndexed { index, item ->
            drawSceneObject(item, index, w, h)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSceneObject(
    item: String,
    index: Int,
    w: Float,
    h: Float
) {
    val x = w * (0.14f + index * 0.16f)
    val y = h * (0.34f + (index % 2) * 0.14f)
    when (item) {
        "screen", "whiteboard", "sign" -> drawRoundRect(
            color = Color(0xFF26364A).copy(alpha = 0.86f),
            topLeft = Offset(x, y),
            size = Size(w * 0.14f, h * 0.12f)
        )

        "suitcase", "bag" -> {
            drawRoundRect(
                color = PracticeColors.Sky.copy(alpha = 0.84f),
                topLeft = Offset(x, y + h * 0.05f),
                size = Size(w * 0.1f, h * 0.12f)
            )
            drawLine(Color.White.copy(alpha = 0.58f), Offset(x + w * 0.03f, y + h * 0.05f), Offset(x + w * 0.03f, y), 3f)
            drawLine(Color.White.copy(alpha = 0.58f), Offset(x + w * 0.07f, y + h * 0.05f), Offset(x + w * 0.07f, y), 3f)
        }

        "chair" -> {
            drawRoundRect(
                color = Color(0xFF536B62).copy(alpha = 0.78f),
                topLeft = Offset(x, y + h * 0.04f),
                size = Size(w * 0.12f, h * 0.08f)
            )
            drawLine(Color(0xFF536B62), Offset(x + w * 0.02f, y + h * 0.12f), Offset(x, y + h * 0.18f), 4f)
            drawLine(Color(0xFF536B62), Offset(x + w * 0.1f, y + h * 0.12f), Offset(x + w * 0.12f, y + h * 0.18f), 4f)
        }

        "bookshelf" -> {
            drawRoundRect(
                color = Color(0xFF5E4B3B).copy(alpha = 0.82f),
                topLeft = Offset(x, y - h * 0.02f),
                size = Size(w * 0.15f, h * 0.2f)
            )
            repeat(3) { shelf ->
                drawLine(
                    Color.White.copy(alpha = 0.45f),
                    Offset(x + w * 0.01f, y + h * (0.03f + shelf * 0.05f)),
                    Offset(x + w * 0.14f, y + h * (0.03f + shelf * 0.05f)),
                    3f
                )
            }
        }

        "book" -> drawRoundRect(
            color = PracticeColors.Mint.copy(alpha = 0.78f),
            topLeft = Offset(x, y + h * 0.04f),
            size = Size(w * 0.1f, h * 0.12f)
        )

        "card" -> drawRoundRect(
            color = Color.White.copy(alpha = 0.72f),
            topLeft = Offset(x, y + h * 0.08f),
            size = Size(w * 0.12f, h * 0.06f)
        )

        "display", "storefront", "counter", "desk", "table" -> drawRoundRect(
            color = PracticeColors.Amber.copy(alpha = 0.58f),
            topLeft = Offset(x, y + h * 0.06f),
            size = Size(w * 0.14f, h * 0.08f)
        )
    }
}

private fun parseSceneColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrElse {
    Color(0xFFE7F0EE)
}

@Composable
private fun TopStageBar(
    scenario: PracticeScenario,
    voiceState: VoiceUiState,
    onBackHome: () -> Unit,
    onToggleVoiceMode: () -> Unit,
    onToggleTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        StageTextButton(text = "< 结束练习", onClick = onBackHome)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = scenario.name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${scenario.level} · ${scenario.estimatedMinutes} 分钟 · ${scenario.sceneTone}",
                color = Color.White.copy(alpha = 0.74f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StageIconButton(
                text = voiceState.modeAction,
                iconRes = R.drawable.ic_demo_mode,
                onClick = onToggleVoiceMode
            )
            StageIconButton(
                text = voiceState.ttsAction,
                iconRes = if (voiceState.ttsEnabled) R.drawable.ic_volume_off else R.drawable.ic_speaker,
                onClick = onToggleTts
            )
        }
    }
}

@Composable
private fun ConversationHistoryCard(
    messages: List<ConversationMessage>,
    isListening: Boolean,
    uiState: PracticeUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = Color(0xCC251A16),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.05f))
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            messages.forEach { message ->
                ConversationMessageRow(message)
            }
            if (isListening || uiState.phase == PracticeState.Recognizing) {
                AudioWaveformStrip(isActive = true)
            }
        }
    }
}

@Composable
private fun ConversationMessageRow(message: ConversationMessage) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = message.speaker,
            color = if (message.fromCoach) Color.White.copy(alpha = 0.72f) else PracticeColors.Amber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message.text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis
        )
        message.secondaryText?.let { secondary ->
            Text(
                text = secondary,
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AudioWaveformStrip(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "voice-wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720),
            repeatMode = RepeatMode.Restart
        ),
        label = "voice-wave-phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val centerY = size.height / 2f
        val bars = 28
        val step = size.width / bars
        repeat(bars) { index ->
            val baseRatio = ((index * 37) % 11 + 3) / 14f
            val wave = ((sin((phase * 2 * PI) + index * 0.65) + 1) / 2).toFloat()
            val ratio = if (isActive) {
                (0.26f + wave * 0.74f).coerceIn(0.24f, 1f)
            } else {
                baseRatio
            }
            val barHeight = size.height * ratio
            drawLine(
                color = Color.White.copy(alpha = if (isActive) 0.82f else 0.58f),
                start = Offset(step * index + step / 2f, centerY - barHeight / 2f),
                end = Offset(step * index + step / 2f, centerY + barHeight / 2f),
                strokeWidth = 2.5f
            )
        }
    }
}

@Composable
private fun CoachBubble(
    text: String,
    translation: String,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF7F2EA),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Column(
                modifier = Modifier.padding(end = 26.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = text,
                    color = PracticeColors.Ink,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                if (translation.isNotBlank()) {
                    Text(
                        text = translation,
                        color = PracticeColors.Ink.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_speaker),
                contentDescription = "重播回复",
                tint = PracticeColors.Sky,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clickable(onClick = onSpeak)
            )
        }
    }
}

@Composable
private fun PracticeCloudErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xD8241A14),
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "云端连接失败",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(message, color = Color.White.copy(alpha = 0.82f))
                Text(
                    text = "知道了",
                    color = PracticeColors.Amber,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ScoreStrip(
    uiState: PracticeUiState,
    modifier: Modifier = Modifier
) {
    val scores = uiState.turnResult?.scores
    Row(
        modifier = modifier
            .background(Color(0xB01D1511), RoundedCornerShape(18.dp))
            .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreMetric("语法", scores?.grammar?.score)
        ScoreMetric("流利度", scores?.fluency?.score)
        ScoreMetric("语言流畅", scores?.pronunciation?.score)
        ScoreMetric("场景完成度", scores?.completion?.score)
    }
}

@Composable
private fun ScoreMetric(label: String, score: Int?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.76f), style = MaterialTheme.typography.bodySmall)
        Text(
            text = score?.toString() ?: "--",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun BottomPracticeDock(
    uiState: PracticeUiState,
    voiceState: VoiceUiState,
    backendState: CoachBackendUiState,
    isMicPressed: Boolean,
    onPrimaryAction: () -> Unit,
    onStartSpeech: () -> Unit,
    onStartLongSpeech: () -> Unit,
    onMicPressChanged: (Boolean) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceInputActive = voiceState.isListening || isMicPressed

    Box(
        modifier = modifier.background(Color(0xB4261A13)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StageDockButton(
                text = if (backendState.isChecking) "分析中..." else uiState.primaryAction,
                onClick = onPrimaryAction
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HoldToTalkButton(
                    isActive = voiceInputActive,
                    inputLocked = voiceState.isListening,
                    onStartSpeech = onStartSpeech,
                    onStartLongSpeech = onStartLongSpeech,
                    onMicPressChanged = onMicPressChanged
                )
                Text(
                    text = if (voiceInputActive) "正在说话中..." else "按住说话",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.offset(y = (-2).dp)
                )
            }
            StageDockButton(
                text = if (uiState.canFinish) "结束本轮" else "跳过本轮",
                onClick = if (uiState.canFinish) onFinish else onPrimaryAction
            )
        }
    }
}

@Composable
private fun HoldToTalkButton(
    isActive: Boolean,
    inputLocked: Boolean,
    onStartSpeech: () -> Unit,
    onStartLongSpeech: () -> Unit,
    onMicPressChanged: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .size(58.dp)
            .pointerInput(inputLocked, onStartSpeech, onStartLongSpeech, onMicPressChanged) {
                if (inputLocked) return@pointerInput
                detectTapGestures(
                    onPress = {
                        onMicPressChanged(true)
                        try {
                            tryAwaitRelease()
                        } finally {
                            onMicPressChanged(false)
                        }
                    },
                    onTap = { onStartSpeech() },
                    onLongPress = { onStartLongSpeech() }
                )
            },
        color = if (isActive) Color(0xFF2F5FB8) else PracticeColors.Sky,
        contentColor = Color.White,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isActive) {
                AudioWaveformStrip(
                    isActive = true,
                    modifier = Modifier.width(36.dp)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_mic),
                    contentDescription = "语音输入",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun StageDockButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.pointerInput(text, onClick) {
            detectTapGestures(onTap = { onClick() })
        },
        color = Color.White.copy(alpha = 0.12f),
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StageTextButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.pointerInput(text, onClick) {
            detectTapGestures(onTap = { onClick() })
        },
        color = Color.Black.copy(alpha = 0.18f),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StageIconButton(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.pointerInput(text, onClick) {
            detectTapGestures(onTap = { onClick() })
        },
        color = Color.Black.copy(alpha = 0.2f),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
        HeaderIconAction(text = "首页", iconRes = R.drawable.ic_home, onClick = onBackHome)
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text("练习总结", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "${scenario.name} · ${scenario.level} · ${summary.turnCount} 轮完成",
                    color = Color.White.copy(alpha = 0.74f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderIconAction(text = "首页", iconRes = R.drawable.ic_home, onClick = onBackHome)
                HeaderIconAction(text = "分享报告", iconRes = R.drawable.ic_summary, onClick = onBackHome)
            }
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryScoreDashboard(
                scenario = scenario,
                summary = summary,
                modifier = Modifier.weight(1.1f).fillMaxHeight()
            )
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryInsightCard(title = "高频问题", items = summary.improvements)
                SummaryInsightCard(title = "推荐表达", items = summary.practicePlan)
            }
            Column(modifier = Modifier.weight(0.82f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryInsightCard(title = "本次场景", items = listOf(scenario.name, "练习时长 08:32", "${summary.turnCount} 轮完成"))
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("下次目标", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(summary.nextGoal, color = Color.White.copy(alpha = 0.78f))
                        PrimaryAction(text = "再次练习", onClick = onPracticeAgain, modifier = Modifier.fillMaxWidth())
                    }
                }
                summary.turnReviews.firstOrNull()?.let { review ->
                    GlassPanel(modifier = Modifier.fillMaxWidth()) {
                        SummaryTurnReviewCard(review)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderIconAction(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black.copy(alpha = 0.22f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun SummaryScoreDashboard(
    scenario: PracticeScenario,
    summary: PracticeSummary,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(modifier = Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("综合得分", color = Color.White.copy(alpha = 0.78f), fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${summary.averageScore}", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Text("/100", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
                }
                Text("良好", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
                Text("你超过了 72% 的学习者！", color = Color.White.copy(alpha = 0.76f))
            }
            Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("能力雷达", color = Color.White, fontWeight = FontWeight.Bold)
                AbilityRadarChart(
                    scores = summary.scoreBreakdown,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(154.dp)
                )
                Text(scenario.name, color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SummaryInsightCard(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            items.take(2).forEach { item ->
                Text(
                    text = "• $item",
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SummaryTurnReviewCard(
    review: SummaryTurnReview
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "第 ${review.index} 轮 · ${review.score} 分",
            color = PracticeColors.Amber,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "你说：${review.userText}",
            color = Color.White.copy(alpha = 0.78f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "优化：${review.betterExpression}",
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (review.tips.isNotEmpty()) {
            Text(
                "提示：${review.tips.joinToString("；")}",
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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

private fun initialConversationMessages(scenario: PracticeScenario): List<ConversationMessage> = listOf(
    ConversationMessage(
        speaker = "Coach",
        text = scenario.opening,
        secondaryText = scenario.openingTranslation.takeIf { it.isNotBlank() },
        fromCoach = true
    )
)

private fun durationLabelFor(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(1)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

private fun conversationMessagesFor(
    baseMessages: List<ConversationMessage>,
    uiState: PracticeUiState,
    isMicPressed: Boolean
): List<ConversationMessage> {
    val liveUserMessage = when {
        isMicPressed || uiState.phase == PracticeState.Listening -> ConversationMessage(
            speaker = "You",
            text = "正在说话中..."
        )
        uiState.phase == PracticeState.Recognizing && uiState.transcript.isNotBlank() -> ConversationMessage(
            speaker = "You",
            text = uiState.transcript
        )
        uiState.phase == PracticeState.Thinking && uiState.transcript.isNotBlank() -> ConversationMessage(
            speaker = "You",
            text = uiState.transcript,
            secondaryText = "教练正在分析..."
        )
        else -> null
    }

    return if (liveUserMessage == null) baseMessages else baseMessages + liveUserMessage
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
