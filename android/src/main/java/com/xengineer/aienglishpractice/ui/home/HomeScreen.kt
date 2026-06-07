package com.xengineer.aienglishpractice.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import com.xengineer.aienglishpractice.R
import com.xengineer.aienglishpractice.core.CoachCharacterState
import com.xengineer.aienglishpractice.core.HomeDashboard
import com.xengineer.aienglishpractice.ui.practice.CoachCharacterPanel
import com.xengineer.aienglishpractice.ui.shared.GlassPanel
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun HomeScreen(
    dashboard: HomeDashboard,
    onStartPractice: (String) -> Unit,
    onCreateCustomScenario: (String) -> Unit,
    onOpenScenarios: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    StageScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ScenarioSelectionHero(
                dashboard = dashboard,
                onOpenHistory = onOpenHistory,
                onOpenSettings = onOpenSettings
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScenarioTileGrid(
                    onStartPractice = onStartPractice,
                    onCreateCustomScenario = onCreateCustomScenario,
                    onOpenScenarios = onOpenScenarios,
                    modifier = Modifier.weight(1.25f)
                )
                CoachCharacterPanel(
                    state = CoachCharacterState.Idle,
                    showLabel = false,
                    modifier = Modifier
                        .weight(0.82f)
                        .height(300.dp)
                )
            }
            HomeStatsBar(
                dashboard = dashboard,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScenarioSelectionHero(
    dashboard: HomeDashboard,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3E5D8)),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", color = PracticeColors.Cafe, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Hi，今天想练习什么场景？",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "选择一个场景，开启你的英语口语练习吧！",
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text("连续练习", color = Color.White.copy(alpha = 0.74f), style = MaterialTheme.typography.bodySmall)
                Text("${dashboard.practiceStats.streakDays} 天", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            HomeTopAction(text = "记录", iconRes = R.drawable.ic_history, onClick = onOpenHistory)
            Spacer(Modifier.width(8.dp))
            HomeTopAction(text = "设置", iconRes = R.drawable.ic_settings, onClick = onOpenSettings)
        }
    }
}

@Composable
private fun HomeTopAction(
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
private fun ScenarioTileGrid(
    onStartPractice: (String) -> Unit,
    onCreateCustomScenario: (String) -> Unit,
    onOpenScenarios: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tiles = listOf(
        ScenarioTileSpec("面试", "求职面试\n自我介绍", R.drawable.ic_briefcase, "interview", null),
        ScenarioTileSpec("点餐", "餐厅点餐\n交流", R.drawable.ic_restaurant, "restaurant", null),
        ScenarioTileSpec("会议", "团队会议\n沟通", R.drawable.ic_meeting, "meeting", null),
        ScenarioTileSpec("机场", "值机改签\n问路", R.drawable.ic_airport, null, "机场改签"),
        ScenarioTileSpec("购物", "商务购物\n询价", R.drawable.ic_shopping, null, "购物讨价"),
        ScenarioTileSpec("自定义", "自定义场景\n即时生成", R.drawable.ic_custom_scene, null, null, opensScenarioList = true)
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            tiles.take(3).forEach { tile ->
                ScenarioTile(tile, Modifier.weight(1f)) {
                    openTile(tile, onStartPractice, onCreateCustomScenario, onOpenScenarios)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            tiles.drop(3).forEach { tile ->
                ScenarioTile(tile, Modifier.weight(1f)) {
                    openTile(tile, onStartPractice, onCreateCustomScenario, onOpenScenarios)
                }
            }
        }
    }
}

private fun openTile(
    tile: ScenarioTileSpec,
    onStartPractice: (String) -> Unit,
    onCreateCustomScenario: (String) -> Unit,
    onOpenScenarios: () -> Unit
) {
    when {
        tile.opensScenarioList -> onOpenScenarios()
        tile.scenarioId != null -> onStartPractice(tile.scenarioId)
        tile.prompt != null -> onCreateCustomScenario(tile.prompt)
    }
}

@Composable
private fun ScenarioTile(
    spec: ScenarioTileSpec,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(108.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.22f), Color.Black.copy(alpha = 0.28f))))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(spec.iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(spec.title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    spec.subtitle,
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.74f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HomeStatsBar(
    dashboard: HomeDashboard,
    modifier: Modifier = Modifier
) {
    val todayGoalProgress = dashboard.practiceStats.todayCompletedSessions.coerceIn(0, 1)

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassPanel(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_goal),
                    contentDescription = null,
                    tint = PracticeColors.Amber,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("今日目标", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("完成一次练习", color = Color.White.copy(alpha = 0.75f))
                }
                Text("已完成 $todayGoalProgress/1", color = Color.White)
            }
        }
        GlassPanel(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_stats),
                    contentDescription = null,
                    tint = PracticeColors.Amber,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(14.dp))
                Text("${dashboard.practiceStats.completedTurns} 次练习", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(18.dp))
                Text("${dashboard.recentHistory?.averageScore ?: 85} 平均分", color = Color.White.copy(alpha = 0.82f))
            }
        }
    }
}

private data class ScenarioTileSpec(
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    val scenarioId: String?,
    val prompt: String?,
    val opensScenarioList: Boolean = false
)
