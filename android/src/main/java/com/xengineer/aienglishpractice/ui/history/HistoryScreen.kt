package com.xengineer.aienglishpractice.ui.history

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import com.xengineer.aienglishpractice.R
import com.xengineer.aienglishpractice.core.PracticeHistoryEntry
import com.xengineer.aienglishpractice.ui.shared.AbilityRadarChart
import com.xengineer.aienglishpractice.ui.shared.GlassAction
import com.xengineer.aienglishpractice.ui.shared.GlassPanel
import com.xengineer.aienglishpractice.ui.shared.PrimaryAction
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun HistoryScreen(
    entries: List<PracticeHistoryEntry>,
    onStartPractice: (String) -> Unit,
    onOpenReview: (String) -> Unit,
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
                HistoryTimelineList(
                    entries = entries,
                    onOpenReview = onOpenReview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 18.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("共 ${entries.size} 条记录", color = Color.White.copy(alpha = 0.7f))
                GlassAction(text = "清空记录", onClick = onClearHistory)
            }
        }
    }
}

@Composable
private fun HistoryHeader(onBackHome: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBackHome) { Text("<", color = Color.White) }
            Text("历史记录", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text("全部场景", color = Color.White.copy(alpha = 0.82f))
    }
}

@Composable
private fun EmptyHistory(onStartPractice: () -> Unit, modifier: Modifier = Modifier) {
    GlassPanel(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("还没有完成的练习", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("完成一次练习后，这里会显示分数、时长和下次目标。", color = Color.White.copy(alpha = 0.72f))
            Spacer(Modifier.height(18.dp))
            PrimaryAction(text = "开始点餐", onClick = onStartPractice)
        }
    }
}

@Composable
private fun HistoryTimelineList(
    entries: List<PracticeHistoryEntry>,
    onOpenReview: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(entries) { index, entry ->
            HistoryTimelineRow(
                entry = entry,
                rank = index,
                onOpenReview = { onOpenReview(entry.id) }
            )
        }
    }
}

@Composable
private fun HistoryTimelineRow(
    entry: PracticeHistoryEntry,
    rank: Int,
    onOpenReview: () -> Unit
) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(historyColor(rank), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(scenarioIconFor(entry.scenarioId)),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(entry.scenarioName, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(entry.completedAtDisplayLabel(), color = Color.White.copy(alpha = 0.68f))
                }
            }
            Text("${entry.averageScore}", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(10.dp))
            Text("↑ ${entry.turnCount + 2}", color = PracticeColors.Mint, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(28.dp))
            Text(entry.durationLabel.ifBlank { "未记录" }, color = Color.White.copy(alpha = 0.82f))
            Spacer(Modifier.size(16.dp))
            PrimaryAction(text = "回顾", onClick = onOpenReview)
            Spacer(Modifier.size(8.dp))
            Icon(
                painter = painterResource(R.drawable.ic_play_circle),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.78f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun HistoryReviewScreen(
    entry: PracticeHistoryEntry,
    onBackHistory: () -> Unit
) {
    val summary = entry.toSummary()

    StageScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HistoryReviewHeader(entry = entry, onBackHistory = onBackHistory)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassPanel(modifier = Modifier.weight(0.9f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("综合得分", color = Color.White.copy(alpha = 0.78f), fontWeight = FontWeight.Bold)
                        Text("${entry.averageScore}", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                        Text("${entry.turnCount} 轮完成 · ${entry.durationLabel.ifBlank { "未记录时长" }}", color = Color.White.copy(alpha = 0.72f))
                    }
                }
                GlassPanel(modifier = Modifier.weight(1.1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("能力雷达", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
                        AbilityRadarChart(
                            scores = summary.scoreBreakdown,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                GlassPanel(modifier = Modifier.weight(1.1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("下次目标", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
                        Text(summary.nextGoal, color = Color.White.copy(alpha = 0.82f))
                        Text("这是历史回顾，不会重新开启练习。", color = Color.White.copy(alpha = 0.58f))
                    }
                }
            }
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_summary),
                        contentDescription = null,
                        tint = PracticeColors.Amber,
                        modifier = Modifier.size(28.dp)
                    )
                    Text("历史回顾", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("查看已完成练习的分数、优势和下一步目标。", color = Color.White.copy(alpha = 0.72f))
                }
            }
        }
    }
}

@Composable
private fun HistoryReviewHeader(
    entry: PracticeHistoryEntry,
    onBackHistory: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBackHistory) { Text("<", color = Color.White) }
            Text("历史回顾", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text(entry.scenarioName, color = Color.White.copy(alpha = 0.82f))
    }
}

@DrawableRes
private fun scenarioIconFor(scenarioId: String): Int = when (scenarioId) {
    "interview" -> R.drawable.ic_briefcase
    "meeting" -> R.drawable.ic_meeting
    "restaurant" -> R.drawable.ic_restaurant
    else -> R.drawable.ic_custom_scene
}

private fun historyColor(rank: Int): Color = listOf(
    Color(0xFF9D7041),
    Color(0xFF587EA4),
    Color(0xFF5C8B76),
    Color(0xFFC78568)
)[rank % 4]
