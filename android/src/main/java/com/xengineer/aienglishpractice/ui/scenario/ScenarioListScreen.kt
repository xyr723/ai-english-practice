package com.xengineer.aienglishpractice.ui.scenario

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import com.xengineer.aienglishpractice.R
import com.xengineer.aienglishpractice.core.PracticeScenario
import com.xengineer.aienglishpractice.ui.shared.DarkPanel
import com.xengineer.aienglishpractice.ui.shared.GlassPanel
import com.xengineer.aienglishpractice.ui.shared.PrimaryAction
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun ScenarioListScreen(
    scenarios: List<PracticeScenario>,
    onOpenDetail: (String) -> Unit,
    onCreateCustomScenario: (String) -> Unit,
    onBackHome: () -> Unit
) {
    var customPrompt by remember { mutableStateOf("") }

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
                    modifier = Modifier
                        .weight(1.25f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomScenePromptPanel(
                        prompt = customPrompt,
                        onPromptChange = { customPrompt = it },
                        onCreateCustomScenario = {
                            val prompt = customPrompt.trim()
                            if (prompt.isNotBlank()) {
                                onCreateCustomScenario(prompt)
                            }
                        }
                    )
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
private fun CustomScenePromptPanel(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onCreateCustomScenario: () -> Unit
) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "自定义场景",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "输入“机场改签”等真实场景，立即生成匹配的场景背景和角色脚本。",
                color = Color.White.copy(alpha = 0.78f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    label = { Text("请输入自定义场景") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White.copy(alpha = 0.86f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.66f),
                        cursorColor = PracticeColors.Amber,
                        focusedBorderColor = PracticeColors.Amber,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.36f),
                        focusedContainerColor = Color.Black.copy(alpha = 0.18f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.18f)
                    ),
                    modifier = Modifier.weight(1f)
                )
                PrimaryAction(text = "生成场景", onClick = onCreateCustomScenario)
            }
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
        ScenarioHeaderAction(text = "首页", iconRes = R.drawable.ic_home, onClick = onBackHome)
    }
}

@Composable
private fun ScenarioHeaderAction(
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
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ScenarioRow(
    scenario: PracticeScenario,
    onClick: () -> Unit
) {
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(PracticeColors.Amber.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(scenarioIconFor(scenario.id)),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(text = scenario.description, color = Color.White.copy(alpha = 0.76f))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ScenarioStat(label = "角色", value = scenario.role)
                    ScenarioStat(label = "等级", value = scenario.level)
                    ScenarioStat(label = "时长", value = "${scenario.estimatedMinutes} 分")
                }
            }
            Spacer(Modifier.width(16.dp))
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.74f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ScenarioStat(label: String, value: String) {
    Column {
        Text(value, color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
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

@DrawableRes
private fun scenarioIconFor(scenarioId: String): Int = when {
    scenarioId.contains("interview") -> R.drawable.ic_briefcase
    scenarioId.contains("meeting") -> R.drawable.ic_meeting
    scenarioId.contains("airport") -> R.drawable.ic_airport
    scenarioId.contains("shopping") -> R.drawable.ic_shopping
    scenarioId.contains("restaurant") -> R.drawable.ic_restaurant
    else -> R.drawable.ic_custom_scene
}
