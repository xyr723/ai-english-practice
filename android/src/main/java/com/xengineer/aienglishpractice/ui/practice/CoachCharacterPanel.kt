package com.xengineer.aienglishpractice.ui.practice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.xengineer.aienglishpractice.core.CoachCharacterState
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun CoachCharacterPanel(
    state: CoachCharacterState,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(112.dp),
    showLabel: Boolean = true
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("lottie/robot/a/Main Scene.json"),
        imageAssetsFolder = "lottie/robot/i/"
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
        if (showLabel) {
            Text(
                text = state.label,
                modifier = Modifier.align(Alignment.BottomCenter),
                color = PracticeColors.Ink,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private val CoachCharacterState.label: String
    get() = when (this) {
        CoachCharacterState.Idle -> "待机"
        CoachCharacterState.Listening -> "倾听中"
        CoachCharacterState.Thinking -> "思考中"
        CoachCharacterState.Speaking -> "回复中"
    }
