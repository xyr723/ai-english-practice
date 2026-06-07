package com.xengineer.aienglishpractice.ui.practice

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeScreenContractTest {
    @Test
    fun practiceControlsDoNotExposeDeveloperOnlyActions() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertFalse(source.contains("模拟异常"))
        assertFalse(source.contains("仅后端"))
    }

    @Test
    fun practiceScreenDistinguishesLongPressSpeechInput() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("onStartLongSpeech"))
        assertTrue(source.contains("SpeechListenMode.Extended"))
    }

    @Test
    fun practiceScreenRendersLottieCharacterModule() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("CoachCharacterPanel"))
        assertTrue(source.contains("CoachCharacterState.from"))
    }

    @Test
    fun lottieCharacterAssetsCoverFourRequiredStates() {
        val rawDir = File("src/main/res/raw")

        assertTrue(File(rawDir, "coach_idle.json").exists())
        assertTrue(File(rawDir, "coach_listening.json").exists())
        assertTrue(File(rawDir, "coach_thinking.json").exists())
        assertTrue(File(rawDir, "coach_speaking.json").exists())
    }

    @Test
    fun lottieComposeDependencyIsDeclared() {
        val source = File("../build.gradle.kts").readText() +
            File("build.gradle.kts").readText()

        assertTrue(source.contains("com.airbnb.android:lottie-compose"))
    }

    @Test
    fun practiceScreenUsesDedicatedEnhancedSummaryPage() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("EnhancedSummaryPage"))
        assertTrue(source.contains("scoreBreakdown"))
        assertTrue(source.contains("turnReviews"))
        assertTrue(source.contains("practicePlan"))
    }

    @Test
    fun practiceScreenUsesEngineSelectionForRuntimeLoop() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("engineSelectionConfig: EngineSelectionConfig = EngineSelectionConfig.default()"))
        assertTrue(source.contains("engineSelectionConfig.preferredBackendMode"))
        assertFalse(source.contains("engineSelectionConfig.runtimeSummaries"))
    }

    @Test
    fun practiceScreenUsesImmersiveReferenceStageLayout() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("ImmersivePracticeStage"))
        assertTrue(source.contains("SceneBackgroundImage"))
        assertTrue(source.contains("painterResource"))
        assertTrue(source.contains("HoldToTalkButton"))
        assertTrue(source.contains("ScoreStrip"))
    }

    @Test
    fun practiceScreenUsesSceneImageAndMicVectorInsteadOfCanvasRingsAndTextMic() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("backgroundDrawableFor"))
        assertTrue(source.contains("GeneratedSceneBackground"))
        assertTrue(source.contains("backgroundDrawableFor(descriptor)?.let"))
        assertTrue(source.contains("R.drawable.ic_mic"))
        assertFalse(source.contains("Text(\"麦\""))
        assertTrue(source.contains("R.drawable.bg_restaurant_photo"))
        assertTrue(source.contains("R.drawable.bg_airport_photo"))
        assertTrue(File("src/main/res/drawable-nodpi/bg_restaurant_photo.png").exists())
        assertTrue(File("src/main/res/drawable-nodpi/bg_airport_photo.png").exists())
        assertTrue(File("src/main/res/drawable/ic_mic.xml").exists())
    }

    @Test
    fun customSceneBackgroundUsesPhotoBackedVisualTheme() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("when (descriptor.visualTheme)"))
        assertFalse(source.contains("when (descriptor.theme)"))
    }

    @Test
    fun practiceTopActionsUseVectorIcons() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()
        val voiceSource = File("src/main/java/com/xengineer/aienglishpractice/core/VoiceUiState.kt").readText()

        assertTrue(source.contains("StageIconButton"))
        assertTrue(source.contains("R.drawable.ic_demo_mode"))
        assertTrue(source.contains("R.drawable.ic_volume_off"))
        assertFalse(source.contains("text = \"朗读\""))
        assertTrue(voiceSource.contains("关闭自动朗读"))
        assertTrue(voiceSource.contains("打开自动朗读"))
    }

    @Test
    fun practiceStageDoesNotExposeTechnicalRuntimeStatusInline() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertFalse(source.contains("text = backendState.statusText"))
        assertFalse(source.contains("engineSelectionConfig.profile.title"))
        assertFalse(source.contains("engineSelectionConfig.runtimeSummaries.take"))
        assertFalse(source.contains("ASR：Android"))
        assertFalse(source.contains("稳定演示"))
        assertTrue(source.contains("PracticeCloudErrorDialog"))
        assertTrue(source.contains("云端连接失败"))
        assertTrue(source.contains("cloudErrorMessage"))
    }

    @Test
    fun cloudErrorDialogIsClearedBeforeRetryAndAfterBackendSuccess() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("cloudErrorMessage = null\n        val activeBackendState = backendState.withChecking()"))
        assertTrue(source.contains("backendState = activeBackendState.withBackendSuccess(backendResult.source)\n                cloudErrorMessage = null"))
    }

    @Test
    fun practiceStageUsesScrollableConversationHistoryInsteadOfSingleTurnPanel() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("ConversationHistoryCard"))
        assertTrue(source.contains("ConversationMessage"))
        assertTrue(source.contains("rememberScrollState"))
        assertTrue(source.contains("verticalScroll"))
        assertTrue(source.contains("top = if (compact) 58.dp else 68.dp"))
        assertFalse(source.contains("TranscriptCard("))
    }

    @Test
    fun speechInputUsesAnimatedWaveformAndListeningCopy() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("rememberInfiniteTransition"))
        assertTrue(source.contains("animateFloat"))
        assertTrue(source.contains("isActive: Boolean"))
        assertTrue(source.contains("inputLocked: Boolean"))
        assertTrue(source.contains("isMicPressed"))
        assertTrue(source.contains("onMicPressChanged"))
        assertTrue(source.contains("正在说话中"))
    }

    @Test
    fun coachReplyBubbleHasInlineReplaySpeakerButton() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("CoachBubble("))
        assertTrue(source.contains("onSpeak: () -> Unit"))
        assertTrue(source.contains("R.drawable.ic_speaker"))
        assertTrue(source.contains("contentDescription = \"重播回复\""))
    }

    @Test
    fun speechErrorDoesNotReplaceCoachDialogueWithRecoveryCopy() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("PracticeState.Error -> opening"))
        assertFalse(source.contains("恢复练习后，可继续当前场景。"))
    }

    @Test
    fun speechRecognitionErrorNeverSubmitsFixedDemoTranscript() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("val recognizedTranscript = voiceState.bestTranscript"))
        assertTrue(source.contains("submitRecognizedSpeech(recognizedTranscript)"))
        assertFalse(source.contains("val fallbackTranscript = demoTranscript"))
        assertFalse(source.contains("showFeedback(fallbackTranscript)"))
    }

    @Test
    fun speechFinalResultAutomaticallySubmitsForCoachFeedback() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()

        assertTrue(source.contains("submitRecognizedSpeech"))
        assertTrue(source.contains("onFinal = { text ->"))
        assertTrue(source.contains("submitRecognizedSpeech(transcript)"))
    }

    @Test
    fun coachCharacterPanelCanRenderAsLargeStageAvatar() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/CoachCharacterPanel.kt").readText()

        assertTrue(source.contains("showLabel: Boolean = true"))
        assertTrue(source.contains("Modifier.fillMaxSize()"))
    }

    @Test
    fun coachCharacterPanelUsesDownloadedRobotAssetWithImageAssets() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/CoachCharacterPanel.kt").readText()

        assertTrue(source.contains("LottieCompositionSpec.Asset(\"lottie/robot/a/Main Scene.json\")"))
        assertTrue(source.contains("imageAssetsFolder = \"lottie/robot/i/\""))
        assertTrue(File("src/main/assets/lottie/robot/a/Main Scene.json").exists())
        assertTrue(File("src/main/assets/lottie/robot/i/image_0.png").exists())
    }

    @Test
    fun summaryPageUsesReferenceDashboardLayout() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/practice/PracticeScreen.kt").readText()
        val radarSource = File("src/main/java/com/xengineer/aienglishpractice/ui/shared/AbilityRadarChart.kt").readText()

        assertTrue(source.contains("SummaryScoreDashboard"))
        assertTrue(source.contains("AbilityRadarChart"))
        assertTrue(source.contains("SummaryInsightCard"))
        assertTrue(source.contains("综合得分"))
        assertTrue(source.contains("再次练习"))
        assertTrue(source.contains("HeaderIconAction"))
        assertTrue(source.contains("R.drawable.ic_home"))
        assertTrue(source.contains("R.drawable.ic_summary"))
        assertTrue(source.contains("modifier = Modifier.fillMaxSize()"))
        assertTrue(source.contains("items.take(2).forEach"))
        assertTrue(radarSource.contains("RadarAxisLabel("))
        assertTrue(radarSource.contains("Modifier.align(Alignment.TopCenter)"))
        assertTrue(radarSource.contains("Modifier.align(Alignment.CenterEnd)"))
        assertTrue(radarSource.contains("Modifier.align(Alignment.CenterStart)"))
        assertFalse(source.contains("SummaryScoreRow(score.label, score.score)"))
        assertTrue(source.contains("summary.turnReviews.firstOrNull()?.let"))
        assertFalse(source.contains("summary.turnReviews.take(2).forEach"))
    }

    @Test
    fun summaryRadarUsesFiveScoreDimensions() {
        val source = File("src/main/java/com/xengineer/aienglishpractice/ui/shared/AbilityRadarChart.kt").readText()

        assertTrue(source.contains("val values = scores.take(5)"))
        assertFalse(source.contains("scores.take(4)"))
    }
}
