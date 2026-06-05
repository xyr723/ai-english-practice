package com.xengineer.aienglishpractice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xengineer.aienglishpractice.ui.AppRoot
import com.xengineer.aienglishpractice.ui.theme.PracticeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PracticeTheme {
                AppRoot()
            }
        }
    }
}
