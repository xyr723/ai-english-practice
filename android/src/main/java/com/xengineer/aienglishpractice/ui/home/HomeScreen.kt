package com.xengineer.aienglishpractice.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xengineer.aienglishpractice.core.HomeDashboard
import com.xengineer.aienglishpractice.ui.shared.DarkPanel
import com.xengineer.aienglishpractice.ui.shared.LightPanel
import com.xengineer.aienglishpractice.ui.shared.PrimaryAction
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun HomeScreen(
    dashboard: HomeDashboard,
    onStartPractice: (String) -> Unit,
    onOpenScenarios: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    StageScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Lingua Café",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scenario speaking practice",
                        color = Color(0xFFDCEDEA),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row {
                    TextButton(onClick = onOpenHistory) {
                        Text("History", color = Color.White)
                    }
                    TextButton(onClick = onOpenSettings) {
                        Text("Settings", color = Color.White)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                DarkPanel(modifier = Modifier.weight(1.1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Today",
                            color = PracticeColors.Amber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${dashboard.practiceStats.todayGoalMinutes} min speaking goal",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Streak ${dashboard.practiceStats.streakDays} day · ${dashboard.practiceStats.completedTurns} turns completed",
                            color = Color(0xFFEAD7C4)
                        )
                        Spacer(Modifier.height(8.dp))
                        PrimaryAction(
                            text = "Start ${dashboard.primaryScenario.name}",
                            onClick = { onStartPractice(dashboard.primaryScenario.id) }
                        )
                    }
                }

                LightPanel(modifier = Modifier.weight(0.9f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Recommended scene",
                            color = PracticeColors.Ink,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dashboard.primaryScenario.role,
                            color = PracticeColors.Cafe,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dashboard.primaryScenario.opening,
                            color = PracticeColors.Ink
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Metric(label = "Goals", value = dashboard.primaryScenario.goals.size.toString())
                            Metric(label = "Turns", value = dashboard.primaryScenario.turns.size.toString())
                            Metric(label = "Level", value = dashboard.primaryScenario.level)
                            Metric(label = "Time", value = "${dashboard.primaryScenario.estimatedMinutes}m")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Practice politely, get scored, review better expressions.",
                    color = Color(0xFFDCEDEA)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onOpenScenarios) {
                        Text("Scenarios", color = Color.White)
                    }
                    PrimaryAction(text = "Continue", onClick = { onStartPractice(dashboard.primaryScenario.id) })
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column {
        Text(value, color = PracticeColors.Sky, fontWeight = FontWeight.Bold)
        Text(label, color = PracticeColors.Ink)
    }
    Spacer(Modifier.width(4.dp))
}
