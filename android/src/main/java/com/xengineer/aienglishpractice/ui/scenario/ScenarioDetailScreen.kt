package com.xengineer.aienglishpractice.ui.scenario

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
import com.xengineer.aienglishpractice.core.PracticeScenario
import com.xengineer.aienglishpractice.ui.shared.DarkPanel
import com.xengineer.aienglishpractice.ui.shared.LightPanel
import com.xengineer.aienglishpractice.ui.shared.PrimaryAction
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun ScenarioDetailScreen(
    scenario: PracticeScenario,
    onStartPractice: (String) -> Unit,
    onBackList: () -> Unit
) {
    StageScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ScenarioDetailHeader(scenario = scenario, onBackList = onBackList)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                LightPanel(modifier = Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("教练开场", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
                        Text(
                            text = scenario.opening,
                            color = PracticeColors.Cafe,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("练习目标", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
                        scenario.goals.forEachIndexed { index, goal ->
                            Text("${index + 1}. ${goal.readableGoal()}", color = PracticeColors.Ink)
                        }
                        Text("关键词", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
                        Text(scenario.keywords.joinToString(" · "), color = PracticeColors.Ink)
                    }
                }
                DarkPanel(modifier = Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("对话节奏", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
                        scenario.turns.forEach { turn ->
                            Column {
                                Text(turn.expectedIntent.readableGoal(), color = Color.White, fontWeight = FontWeight.Bold)
                                Text(turn.reply, color = Color(0xFFEAD7C4))
                            }
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
                    text = "${scenario.level} · ${scenario.estimatedMinutes} 分钟 · ${scenario.sceneTone}",
                    color = Color(0xFFDCEDEA)
                )
                PrimaryAction(
                    text = "开始练习",
                    onClick = { onStartPractice(scenario.id) }
                )
            }
        }
    }
}

@Composable
private fun ScenarioDetailHeader(
    scenario: PracticeScenario,
    onBackList: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = scenario.name,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = scenario.description, color = Color(0xFFDCEDEA))
        }
        TextButton(onClick = onBackList) {
            Text("场景", color = Color.White)
        }
    }
    Spacer(Modifier.height(8.dp))
}

private fun String.readableGoal(): String = when (this) {
    "order_food_or_drink" -> "点餐或点饮品"
    "use_polite_expression" -> "使用礼貌表达"
    "answer_follow_up_question" -> "回答追问"
    "confirm_option" -> "确认选项"
    "introduce_self" -> "自我介绍"
    "describe_experience" -> "描述经历"
    "give_opinion" -> "表达观点"
    "ask_clarifying_question" -> "追问细节"
    "confirm_next_step" -> "确认下一步"
    else -> split("_").joinToString(" ")
}
