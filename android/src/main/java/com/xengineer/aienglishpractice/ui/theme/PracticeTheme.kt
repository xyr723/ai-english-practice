package com.xengineer.aienglishpractice.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object PracticeColors {
    val Ink = Color(0xFF182A2E)
    val Paper = Color(0xFFF6F0E8)
    val Cafe = Color(0xFF6F4528)
    val Roast = Color(0xFF271712)
    val Mint = Color(0xFFB9DDD2)
    val Sky = Color(0xFF4C83D9)
    val Amber = Color(0xFFFFC46B)
    val Coral = Color(0xFFE86E55)
    val WhitePanel = Color(0xEAF8F4EF)
    val DarkPanel = Color(0xD7221714)

    val StageBrush = Brush.horizontalGradient(
        listOf(Color(0xFF23110B), Color(0xFF6F4528), Color(0xFF173B42))
    )
}

private val LightScheme: ColorScheme = lightColorScheme(
    primary = PracticeColors.Sky,
    secondary = PracticeColors.Cafe,
    tertiary = PracticeColors.Coral,
    background = PracticeColors.Paper,
    surface = Color(0xFFFFFBF6),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = PracticeColors.Ink,
    onSurface = PracticeColors.Ink
)

private val DarkScheme: ColorScheme = darkColorScheme(
    primary = PracticeColors.Sky,
    secondary = PracticeColors.Amber,
    tertiary = PracticeColors.Mint,
    background = PracticeColors.Roast,
    surface = Color(0xFF30221C),
    onPrimary = Color.White,
    onSecondary = PracticeColors.Ink,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun PracticeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}
