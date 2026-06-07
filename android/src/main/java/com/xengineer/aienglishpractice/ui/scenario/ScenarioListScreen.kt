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
                text = "选择场景，查看目标，然后进入引导式口语练习。",
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
                text = "练习场景",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "选择一个明确的口语语境",
                color = Color(0xFFDCEDEA),
                style = MaterialTheme.typography.titleMedium
            )
        }
        TextButton(onClick = onBackHome) {
            Text("首页", color = Color.White)
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
                    ScenarioStat(label = "角色", value = scenario.role)
                    ScenarioStat(label = "等级", value = scenario.level)
                    ScenarioStat(label = "时长", value = "${scenario.estimatedMinutes} 分")
                }
            }
            Spacer(Modifier.width(16.dp))
            PrimaryAction(text = "详情", onClick = onClick)
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
            Text("练习路径", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(
                text = "1. 选择场景\n2. 查看目标\n3. 完成一轮对话\n4. 查看总结评分",
                color = Color.White
            )
            Text(
                text = "每个场景都会复用同一套反馈引擎，稳定产出纠错、评分和总结。",
                color = Color(0xFFEAD7C4)
            )
        }
    }
}
