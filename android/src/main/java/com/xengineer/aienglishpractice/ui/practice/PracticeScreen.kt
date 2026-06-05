package com.xengineer.aienglishpractice.ui.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xengineer.aienglishpractice.core.PracticeScenario
import com.xengineer.aienglishpractice.core.PracticeSession
import com.xengineer.aienglishpractice.core.PracticeSummary
import com.xengineer.aienglishpractice.core.RuleCorrectionEngine
import com.xengineer.aienglishpractice.core.ScoreEngine
import com.xengineer.aienglishpractice.core.TurnResult
import com.xengineer.aienglishpractice.ui.shared.DarkPanel
import com.xengineer.aienglishpractice.ui.shared.LightPanel
import com.xengineer.aienglishpractice.ui.shared.PrimaryAction
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun PracticeScreen(
    scenarioId: String,
    onBackHome: () -> Unit
) {
    val scenario = remember(scenarioId) { PracticeScenario.restaurant() }
    val session = remember(scenarioId) {
        PracticeSession(
            scenario = scenario,
            correctionEngine = RuleCorrectionEngine(),
            scoreEngine = ScoreEngine()
        ).also { it.start() }
    }
    var turnResult by remember { mutableStateOf<TurnResult?>(null) }
    var summary by remember { mutableStateOf<PracticeSummary?>(null) }

    StageScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            PracticeHeader(
                title = scenario.name,
                subtitle = "Goal: order politely, answer follow-up questions, and finish the scene.",
                onBackHome = onBackHome
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                FeedbackPanel(turnResult, modifier = Modifier.weight(0.9f))
                CoachPanel(turnResult, summary, modifier = Modifier.weight(1.1f))
            }
            PracticeControls(
                onSubmitDemo = {
                    summary = null
                    turnResult = session.submitTurn(
                        text = "I want order a coffee",
                        durationMs = 6000,
                        asrConfidence = 0.8f
                    )
                },
                onFinish = { summary = session.finish() }
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
private fun FeedbackPanel(turnResult: TurnResult?, modifier: Modifier = Modifier) {
    DarkPanel(modifier = modifier.fillMaxWidth()) {
        Column {
            Text("You said", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(turnResult?.userText ?: "Ready for your first turn.", color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text("Better expression", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(turnResult?.betterExpression ?: "Feedback appears after a turn.", color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text("Tips", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(turnResult?.tips?.joinToString("\n") ?: "No tips yet.", color = Color.White)
        }
    }
}

@Composable
private fun CoachPanel(
    turnResult: TurnResult?,
    summary: PracticeSummary?,
    modifier: Modifier = Modifier
) {
    LightPanel(modifier = modifier.fillMaxWidth()) {
        Column {
            Text("Coach reply", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
            Text(turnResult?.reply ?: "Welcome! What would you like to order today?")
            Spacer(Modifier.height(18.dp))
            ScoreRow("Grammar", turnResult?.scores?.grammar?.score)
            ScoreRow("Fluency", turnResult?.scores?.fluency?.score)
            ScoreRow("Pronunciation", turnResult?.scores?.pronunciation?.score)
            ScoreRow("Completion", turnResult?.scores?.completion?.score)
            if (summary != null) {
                Spacer(Modifier.height(18.dp))
                Text("Summary", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
                Text("Turns: ${summary.turnCount}  Average: ${summary.averageScore}")
                Text(summary.nextGoal)
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
private fun PracticeControls(onSubmitDemo: () -> Unit, onFinish: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        PrimaryAction(text = "Demo Turn", onClick = onSubmitDemo)
        Spacer(Modifier.width(16.dp))
        PrimaryAction(text = "Finish", onClick = onFinish)
    }
}
