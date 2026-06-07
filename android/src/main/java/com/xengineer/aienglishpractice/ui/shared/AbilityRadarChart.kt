package com.xengineer.aienglishpractice.ui.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xengineer.aienglishpractice.core.SummaryScoreBreakdown
import com.xengineer.aienglishpractice.ui.theme.PracticeColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun AbilityRadarChart(
    scores: List<SummaryScoreBreakdown>,
    modifier: Modifier = Modifier
) {
    val values = scores.take(5)
    Box(modifier = modifier.height(154.dp)) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 48.dp, vertical = 26.dp)
        ) {
            if (values.isEmpty()) return@Canvas

            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) * 0.45f
            val count = values.size

            fun point(index: Int, scale: Float): Offset {
                val angle = -PI / 2 + (2 * PI * index / count)
                return Offset(
                    x = center.x + (cos(angle) * radius * scale).toFloat(),
                    y = center.y + (sin(angle) * radius * scale).toFloat()
                )
            }

            listOf(0.33f, 0.66f, 1f).forEach { scale ->
                val grid = Path()
                values.indices.forEach { index ->
                    val p = point(index, scale)
                    if (index == 0) grid.moveTo(p.x, p.y) else grid.lineTo(p.x, p.y)
                }
                grid.close()
                drawPath(grid, Color.White.copy(alpha = 0.16f), style = Stroke(width = 1.4f))
            }

            values.indices.forEach { index ->
                drawLine(
                    color = Color.White.copy(alpha = 0.16f),
                    start = center,
                    end = point(index, 1f),
                    strokeWidth = 1.2f
                )
            }

            val area = Path()
            values.forEachIndexed { index, score ->
                val p = point(index, (score.score.coerceIn(0, 100) / 100f))
                if (index == 0) area.moveTo(p.x, p.y) else area.lineTo(p.x, p.y)
            }
            area.close()
            drawPath(area, PracticeColors.Sky.copy(alpha = 0.42f))
            drawPath(area, PracticeColors.Sky, style = Stroke(width = 2.4f))
        }

        values.getOrNull(0)?.let {
            RadarAxisLabel(score = it, modifier = Modifier.align(Alignment.TopCenter))
        }
        values.getOrNull(1)?.let {
            RadarAxisLabel(score = it, modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-18).dp))
        }
        values.getOrNull(2)?.let {
            RadarAxisLabel(score = it, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp))
        }
        values.getOrNull(3)?.let {
            RadarAxisLabel(score = it, modifier = Modifier.align(Alignment.BottomStart).offset(x = 4.dp))
        }
        values.getOrNull(4)?.let {
            RadarAxisLabel(score = it, modifier = Modifier.align(Alignment.CenterStart).offset(y = (-18).dp))
        }
    }
}

@Composable
private fun RadarAxisLabel(
    score: SummaryScoreBreakdown,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(58.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = score.label,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${score.score}",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
