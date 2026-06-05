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
                text = "练习记录仅保存在当前演示会话中。",
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
                text = "练习记录",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "复盘总结并重复练习",
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
private fun EmptyHistory(onStartPractice: () -> Unit, modifier: Modifier = Modifier) {
    LightPanel(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "还没有完成的练习",
                color = PracticeColors.Ink,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "完成一次练习后，这里会显示总结、平均分和下次目标。",
                color = PracticeColors.Ink
            )
            Spacer(Modifier.height(18.dp))
            PrimaryAction(text = "开始点餐", onClick = onStartPractice)
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
                    text = "平均 ${entry.averageScore} · ${entry.turnCount} 轮",
                    color = PracticeColors.Ink,
                    fontWeight = FontWeight.Bold
                )
                Text(entry.nextGoal, color = PracticeColors.Ink)
            }
            PrimaryAction(text = "再练一次", onClick = onPracticeAgain)
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
            Text("本地总结", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            Text(
                text = "${entries.size} 次练习",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("完成轮次：$totalTurns", color = Color(0xFFEAD7C4))
            Text("平均分：$averageScore", color = Color(0xFFEAD7C4))
            Text("最近场景：${latest.scenarioName}", color = Color(0xFFEAD7C4))
            Spacer(Modifier.height(6.dp))
            PrimaryAction(text = "清空记录", onClick = onClearHistory)
        }
    }
}
