package com.xengineer.aienglishpractice.ui.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.xengineer.aienglishpractice.core.CoachFeedbackSource
import com.xengineer.aienglishpractice.core.PracticeState
import com.xengineer.aienglishpractice.core.PracticeStep
import com.xengineer.aienglishpractice.core.PracticeUiState
import com.xengineer.aienglishpractice.core.PracticeHistoryEntry
import com.xengineer.aienglishpractice.core.PracticeSession
import com.xengineer.aienglishpractice.core.PracticeScenario
import com.xengineer.aienglishpractice.core.RuleCorrectionEngine
import com.xengineer.aienglishpractice.core.ScenarioCatalog
import com.xengineer.aienglishpractice.core.ScoreEngine
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
    var backendState by remember(scenarioId) { mutableStateOf(CoachBackendUiState.initial()) }
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
        TextToSpeechAdapter(context.applicationContext) { ready ->
            ttsReady = ready
        }
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
                backendState = activeBackendState.withBackendSuccess()
                uiState = PracticeUiState.speaking(
                    scenario = scenario,
                    turnResult = backendResult
                )
                if (voiceState.ttsEnabled) {
                    textToSpeech.speak(backendResult.reply)
                }
            } catch (error: Exception) {
                val failedState = activeBackendState.withBackendFailure(
                    error.message ?: "Backend request failed"
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

    fun startSpeechInput() {
        if (!speechRecognizer.isAvailable()) {
            voiceState = voiceState.withRecognitionError("Speech recognition is unavailable on this device.")
            return
        }
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        voiceState = voiceState.useSpeechMode().startListening()
        uiState = PracticeUiState.listening(scenario)
        speechRecognizer.startListening(
            SpeechRecognitionCallbacks(
                onReady = {
                    voiceState = voiceState.useSpeechMode().startListening()
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
                        message = "$message Demo fallback is still available."
                    )
                }
            )
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

    fun toggleBackendMode() {
        backendState = backendState.nextMode()
    }

    StageScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            PracticeHeader(
                title = scenario.name,
                subtitle = "${scenario.level} · ${scenario.estimatedMinutes} min · ${scenario.sceneTone}",
                onBackHome = onBackHome
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
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
                onToggleVoiceMode = { toggleVoiceMode() },
                onToggleTts = { voiceState = voiceState.setTtsEnabled(!voiceState.ttsEnabled) },
                onToggleBackend = { toggleBackendMode() },
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
                },
                onSimulateError = {
                    uiState = PracticeUiState.error(
                        scenario = scenario,
                        message = "Microphone permission is unavailable. Recover and continue with demo mode."
                    )
                }
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
        TextButton(onClick = onBackHome) {
            Text("Home", color = Color.White)
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
    DarkPanel(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Live state", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(
                text = uiState.statusTitle,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(uiState.statusBody, color = Color(0xFFEAD7C4))
            Text("Voice: ${voiceState.statusText}", color = Color(0xFFEAD7C4))
            Text(
                "TTS: ${if (voiceState.ttsReady) "ready" else "initializing"} · ${if (voiceState.ttsEnabled) "enabled" else "muted"}",
                color = Color(0xFFEAD7C4)
            )
            Text("Coach: ${backendState.statusText}", color = Color(0xFFEAD7C4))
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
        step.isComplete -> "done"
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
        uiState.phase == PracticeState.Listening -> "Listening for your answer..."
        uiState.transcript.isNotBlank() -> uiState.transcript
        else -> "Transcript appears after recognition."
    }

    DarkPanel(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("You said", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(transcript, color = Color.White)
            Text("Better expression", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(
                turnResult?.betterExpression ?: "Correction appears after the coach check.",
                color = Color.White
            )
            Text("Tips", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(turnResult?.tips?.joinToString("\n") ?: "No tips yet.", color = Color.White)
        }
    }
}

@Composable
private fun CoachPanel(
    opening: String,
    uiState: PracticeUiState,
    voiceState: VoiceUiState,
    onSpeakCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    val turnResult = uiState.turnResult
    val summary = uiState.summary

    LightPanel(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Coach reply", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
            Text(replyText(opening, uiState))
            Text(
                text = "Source: ${turnResult?.source?.label() ?: "Waiting"}",
                color = PracticeColors.Ink
            )
            ScoreRow("Grammar", turnResult?.scores?.grammar?.score)
            ScoreRow("Fluency", turnResult?.scores?.fluency?.score)
            ScoreRow("Pronunciation", turnResult?.scores?.pronunciation?.score)
            ScoreRow("Completion", turnResult?.scores?.completion?.score)
            if (summary != null) {
                Text("Summary", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
                Text("Turns: ${summary.turnCount}  Average: ${summary.averageScore}")
                Text(summary.nextGoal)
            }
            if (voiceState.ttsEnabled) {
                PrimaryAction(
                    text = if (voiceState.ttsReady) "Speak Coach" else "TTS Loading",
                    onClick = onSpeakCoach
                )
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, score: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(128.dp), color = PracticeColors.Ink)
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
    onToggleVoiceMode: () -> Unit,
    onToggleTts: () -> Unit,
    onToggleBackend: () -> Unit,
    onFinish: () -> Unit,
    onSimulateError: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimaryAction(
                text = if (backendState.isChecking) "Checking..." else uiState.primaryAction,
                onClick = onPrimaryAction,
                modifier = Modifier.widthIn(min = 156.dp)
            )
            Spacer(Modifier.width(12.dp))
            PrimaryAction(
                text = if (voiceState.isListening) "Listening..." else voiceState.speechAction,
                onClick = onStartSpeech,
                modifier = Modifier.widthIn(min = 144.dp)
            )
            if (uiState.canFinish) {
                Spacer(Modifier.width(12.dp))
                PrimaryAction(text = "Finish", onClick = onFinish)
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
            TextButton(onClick = onToggleBackend) {
                Text(backendState.modeAction, color = Color.White)
            }
            if (uiState.phase != PracticeState.Finished) {
                TextButton(onClick = onSimulateError) {
                    Text("Simulate Error", color = Color.White)
                }
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

    PracticeState.Thinking -> "Give me a moment. I am checking your answer."
    PracticeState.Speaking -> uiState.turnResult?.reply ?: opening
    PracticeState.Finished -> "Good work. Review the summary before restarting."
    PracticeState.Error -> "Recover the session, then continue with the same scene."
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
    CoachFeedbackSource.BackendApi -> "Backend API"
    CoachFeedbackSource.LocalFallback -> "Local fallback"
    CoachFeedbackSource.BackendError -> "Backend error"
}
