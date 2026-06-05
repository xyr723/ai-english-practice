package com.xengineer.aienglishpractice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xengineer.aienglishpractice.core.PracticeScenario
import com.xengineer.aienglishpractice.core.PracticeSession
import com.xengineer.aienglishpractice.core.PracticeSummary
import com.xengineer.aienglishpractice.core.RuleCorrectionEngine
import com.xengineer.aienglishpractice.core.ScoreEngine
import com.xengineer.aienglishpractice.core.TurnResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PracticeApp()
            }
        }
    }
}

@Composable
fun PracticeApp() {
    val session = remember {
        PracticeSession(
            scenario = PracticeScenario.restaurant(),
            correctionEngine = RuleCorrectionEngine(),
            scoreEngine = ScoreEngine()
        ).also { it.start() }
    }
    var turnResult by remember { mutableStateOf<TurnResult?>(null) }
    var summary by remember { mutableStateOf<PracticeSummary?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF20110A), Color(0xFF53321E), Color(0xFF122D36))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Header()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                FeedbackPanel(turnResult, modifier = Modifier.weight(0.9f))
                CoachPanel(turnResult, summary, modifier = Modifier.weight(1.1f))
            }
            Controls(
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
private fun Header() {
    Column {
        Text(
            text = "Restaurant Ordering",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Goal: order politely, answer follow-up questions, and finish the scene.",
            color = Color(0xFFD9E7EA)
        )
    }
}

@Composable
private fun FeedbackPanel(turnResult: TurnResult?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xCC1E1714),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("You said", color = Color(0xFFFFD59B), fontWeight = FontWeight.Bold)
            Text(turnResult?.userText ?: "Tap demo turn to simulate speech input.", color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text("Better expression", color = Color(0xFFFFD59B), fontWeight = FontWeight.Bold)
            Text(turnResult?.betterExpression ?: "Feedback will appear after a turn.", color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text("Tips", color = Color(0xFFFFD59B), fontWeight = FontWeight.Bold)
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xDDEEF6F1),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Coach reply", color = Color(0xFF20343A), fontWeight = FontWeight.Bold)
            Text(turnResult?.reply ?: "Welcome! What would you like to order today?")
            Spacer(Modifier.height(18.dp))
            ScoreRow("Grammar", turnResult?.scores?.grammar?.score)
            ScoreRow("Fluency", turnResult?.scores?.fluency?.score)
            ScoreRow("Pronunciation", turnResult?.scores?.pronunciation?.score)
            ScoreRow("Completion", turnResult?.scores?.completion?.score)
            if (summary != null) {
                Spacer(Modifier.height(18.dp))
                Text("Summary", color = Color(0xFF20343A), fontWeight = FontWeight.Bold)
                Text("Turns: ${summary.turnCount}  Average: ${summary.averageScore}")
                Text(summary.nextGoal)
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, score: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(128.dp), color = Color(0xFF20343A))
        Text((score ?: "--").toString(), fontWeight = FontWeight.Bold, color = Color(0xFF20343A))
    }
}

@Composable
private fun Controls(onSubmitDemo: () -> Unit, onFinish: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = onSubmitDemo) {
            Text("Demo Turn")
        }
        Spacer(Modifier.width(16.dp))
        Button(onClick = onFinish) {
            Text("Finish")
        }
    }
}
