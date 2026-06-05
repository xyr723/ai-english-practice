package com.xengineer.aienglishpractice.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xengineer.aienglishpractice.core.PracticeHistoryEntry
import com.xengineer.aienglishpractice.ui.shared.DarkPanel
import com.xengineer.aienglishpractice.ui.shared.LightPanel
import com.xengineer.aienglishpractice.ui.shared.PrimaryAction
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun HistoryScreen(
    entries: List<PracticeHistoryEntry>,
    onStartPractice: (String) -> Unit,
    onClearHistory: () -> Unit,
    onBackHome: () -> Unit
) {
    StageScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HistoryHeader(onBackHome = onBackHome)
            if (entries.isEmpty()) {
                EmptyHistory(
                    onStartPractice = { onStartPractice("restaurant") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 18.dp)
                )
            } else {
                HistoryContent(
                    entries = entries,
                    onStartPractice = onStartPractice,
                    onClearHistory = onClearHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 18.dp)
                )
            }
            Text(
                text = "History is stored locally for the current demo session.",
                color = Color(0xFFDCEDEA)
            )
        }
    }
}

@Composable
private fun HistoryHeader(onBackHome: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "Practice history",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Review summaries and repeat scenes",
                color = Color(0xFFDCEDEA),
                style = MaterialTheme.typography.titleMedium
            )
        }
        TextButton(onClick = onBackHome) {
            Text("Home", color = Color.White)
        }
    }
}

@Composable
private fun EmptyHistory(onStartPractice: () -> Unit, modifier: Modifier = Modifier) {
    LightPanel(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No completed sessions yet",
                color = PracticeColors.Ink,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Finish one practice session to see summary, average score, and next goal here.",
                color = PracticeColors.Ink
            )
            Spacer(Modifier.height(18.dp))
            PrimaryAction(text = "Start Restaurant", onClick = onStartPractice)
        }
    }
}

@Composable
private fun HistoryContent(
    entries: List<PracticeHistoryEntry>,
    onStartPractice: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(
            modifier = Modifier.weight(1.35f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            entries.forEach { entry ->
                HistoryEntryRow(
                    entry = entry,
                    onPracticeAgain = { onStartPractice(entry.scenarioId) }
                )
            }
        }
        HistorySummaryPanel(
            entries = entries,
            onClearHistory = onClearHistory,
            modifier = Modifier.weight(0.75f)
        )
    }
}

@Composable
private fun HistoryEntryRow(
    entry: PracticeHistoryEntry,
    onPracticeAgain: () -> Unit
) {
    LightPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = entry.scenarioName,
                    color = PracticeColors.Cafe,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(entry.completedAtLabel, color = PracticeColors.Ink)
                Text(
                    text = "Average ${entry.averageScore} · ${entry.turnCount} turns",
                    color = PracticeColors.Ink,
                    fontWeight = FontWeight.Bold
                )
                Text(entry.nextGoal, color = PracticeColors.Ink)
            }
            PrimaryAction(text = "Repeat", onClick = onPracticeAgain)
        }
    }
}

@Composable
private fun HistorySummaryPanel(
    entries: List<PracticeHistoryEntry>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTurns = entries.sumOf { entry -> entry.turnCount }
    val averageScore = entries.map { entry -> entry.averageScore }.average().toInt()
    val latest = entries.first()

    DarkPanel(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Local summary", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(
                text = "${entries.size} sessions",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Turns completed: $totalTurns", color = Color(0xFFEAD7C4))
            Text("Average score: $averageScore", color = Color(0xFFEAD7C4))
            Text("Latest scene: ${latest.scenarioName}", color = Color(0xFFEAD7C4))
            Spacer(Modifier.height(6.dp))
            PrimaryAction(text = "Clear History", onClick = onClearHistory)
        }
    }
}
