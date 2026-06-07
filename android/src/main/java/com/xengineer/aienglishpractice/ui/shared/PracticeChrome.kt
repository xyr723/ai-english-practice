package com.xengineer.aienglishpractice.ui.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xengineer.aienglishpractice.R
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

private val FrostedPanelTint = Color(0xC4302018)
private val FrostedPanelHighlight = Color.White.copy(alpha = 0.07f)
private val FrostedPanelBorder = Color.White.copy(alpha = 0.16f)
private val FrostedLightPanelTint = Color(0xDFF7F2EA)
private val FrostedLightPanelHighlight = Color.White.copy(alpha = 0.16f)

@Composable
fun StageScaffold(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PracticeColors.StageBrush)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.bg_restaurant_photo),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.22f))
        )
        content()
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = FrostedPanelTint,
        contentColor = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, FrostedPanelBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            Modifier
                .background(FrostedPanelHighlight)
                .padding(14.dp)
        ) {
            content()
        }
    }
}

@Composable
fun GlassAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.pointerInput(text, onClick) {
            detectTapGestures(onTap = { onClick() })
        },
        color = Color.White.copy(alpha = 0.13f),
        contentColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LightPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = FrostedLightPanelTint,
        contentColor = PracticeColors.Ink,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.32f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            Modifier
                .background(FrostedLightPanelHighlight)
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
fun DarkPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = FrostedPanelTint,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, FrostedPanelBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            Modifier
                .background(FrostedPanelHighlight)
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
fun PrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    if (onLongClick != null) {
        Surface(
            modifier = modifier.pointerInput(onClick, onLongClick) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
            color = PracticeColors.Sky,
            contentColor = Color.White,
            shape = ButtonDefaults.shape
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = PracticeColors.Sky,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit
) {
    StageScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            LightPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(body, style = MaterialTheme.typography.bodyLarge)
                    PrimaryAction(text = action, onClick = onAction)
                }
            }
        }
    }
}
