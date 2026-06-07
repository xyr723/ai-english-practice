package com.xengineer.aienglishpractice.ui.practice

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import com.xengineer.aienglishpractice.R
import com.xengineer.aienglishpractice.core.CoachCharacterState
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun CoachCharacterPanel(
    state: CoachCharacterState,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(state.rawRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(178.dp),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(172.dp)
        )
        Text(
            text = state.label,
            modifier = Modifier.align(Alignment.BottomCenter),
            color = PracticeColors.Ink,
            fontWeight = FontWeight.Bold
        )
    }
}

private val CoachCharacterState.label: String
    get() = when (this) {
        CoachCharacterState.Idle -> "待机"
        CoachCharacterState.Listening -> "倾听中"
        CoachCharacterState.Thinking -> "思考中"
        CoachCharacterState.Speaking -> "回复中"
    }

@get:RawRes
private val CoachCharacterState.rawRes: Int
    get() = when (this) {
        CoachCharacterState.Idle -> R.raw.coach_idle
        CoachCharacterState.Listening -> R.raw.coach_listening
        CoachCharacterState.Thinking -> R.raw.coach_thinking
        CoachCharacterState.Speaking -> R.raw.coach_speaking
    }
