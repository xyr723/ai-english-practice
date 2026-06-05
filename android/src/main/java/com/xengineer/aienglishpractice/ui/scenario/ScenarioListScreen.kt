package com.xengineer.aienglishpractice.ui.scenario

import androidx.compose.foundation.clickable
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
import com.xengineer.aienglishpractice.core.PracticeScenario
import com.xengineer.aienglishpractice.ui.shared.DarkPanel
import com.xengineer.aienglishpractice.ui.shared.LightPanel
import com.xengineer.aienglishpractice.ui.shared.PrimaryAction
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun ScenarioListScreen(
    scenarios: List<PracticeScenario>,
    onOpenDetail: (String) -> Unit,
    onBackHome: () -> Unit
) {
    StageScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ScenarioListHeader(onBackHome = onBackHome)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1.25f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    scenarios.forEach { scenario ->
                        ScenarioRow(
                            scenario = scenario,
                            onClick = { onOpenDetail(scenario.id) }
                        )
                    }
                }
                LearningPathPanel(modifier = Modifier.weight(0.75f))
            }
            Text(
                text = "Pick a scene, review the goals, then start a guided speaking loop.",
                color = Color(0xFFDCEDEA)
            )
        }
    }
}

@Composable
private fun ScenarioListHeader(onBackHome: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "Scenarios",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose a focused speaking context",
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
private fun ScenarioRow(
    scenario: PracticeScenario,
    onClick: () -> Unit
) {
    LightPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.name,
                    color = PracticeColors.Ink,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(text = scenario.description, color = PracticeColors.Ink)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ScenarioStat(label = "Role", value = scenario.role)
                    ScenarioStat(label = "Level", value = scenario.level)
                    ScenarioStat(label = "Time", value = "${scenario.estimatedMinutes} min")
                }
            }
            Spacer(Modifier.width(16.dp))
            PrimaryAction(text = "Details", onClick = onClick)
        }
    }
}

@Composable
private fun ScenarioStat(label: String, value: String) {
    Column {
        Text(value, color = PracticeColors.Cafe, fontWeight = FontWeight.Bold)
        Text(label, color = PracticeColors.Ink, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LearningPathPanel(modifier: Modifier = Modifier) {
    DarkPanel(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Practice path", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(
                text = "1. Choose scene\n2. Read goals\n3. Start demo turn\n4. Finish and review scores",
                color = Color.White
            )
            Text(
                text = "The same feedback engine is reused across scenes, so every session gets correction, scoring, and summary.",
                color = Color(0xFFEAD7C4)
            )
        }
    }
}
