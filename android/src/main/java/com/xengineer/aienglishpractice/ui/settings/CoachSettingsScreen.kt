package com.xengineer.aienglishpractice.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.annotation.DrawableRes
import com.xengineer.aienglishpractice.R
import com.xengineer.aienglishpractice.core.CoachEndpointConfig
import com.xengineer.aienglishpractice.core.CoachEndpointMode
import com.xengineer.aienglishpractice.core.EngineProfileMode
import com.xengineer.aienglishpractice.core.EngineSelectionConfig
import com.xengineer.aienglishpractice.ui.shared.GlassPanel
import com.xengineer.aienglishpractice.ui.shared.PrimaryAction
import com.xengineer.aienglishpractice.ui.shared.StageScaffold
import com.xengineer.aienglishpractice.ui.theme.PracticeColors

@Composable
fun CoachSettingsScreen(
    endpointConfig: CoachEndpointConfig,
    onEndpointConfigChange: (CoachEndpointConfig) -> Unit,
    engineSelectionConfig: EngineSelectionConfig,
    onEngineSelectionChange: (EngineSelectionConfig) -> Unit,
    onBackHome: () -> Unit
) {
    var customInput by remember(endpointConfig.customBaseUrl) {
        mutableStateOf(endpointConfig.customBaseUrl)
    }

    StageScaffold {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsHeader(onBackHome = onBackHome)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingsOptionList(
                    endpointConfig = endpointConfig,
                    engineSelectionConfig = engineSelectionConfig,
                    onEndpointConfigChange = onEndpointConfigChange,
                    onEngineSelectionChange = onEngineSelectionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                )
                Spacer(Modifier.height(12.dp))
                EngineModelSection(
                    engineSelectionConfig = engineSelectionConfig,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("自定义教练地址", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(endpointConfig.baseUrl, color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        OutlinedTextField(
                            value = customInput,
                            onValueChange = { customInput = it },
                            label = { Text("自定义 URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        PrimaryAction(
                            text = "使用自定义地址",
                            onClick = { onEndpointConfigChange(endpointConfig.useCustom(customInput)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun EngineModelSection(
    engineSelectionConfig: EngineSelectionConfig,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("当前使用的模型", color = Color.White, fontWeight = FontWeight.Bold)
            SettingsInfoRow("当前 ASR", engineSelectionConfig.asrEngine.title, R.drawable.ic_mic)
            SettingsInfoRow("当前 TTS", engineSelectionConfig.ttsEngine.title, R.drawable.ic_speaker)
            SettingsInfoRow("当前评测模型", engineSelectionConfig.evaluationEngine.title, R.drawable.ic_settings)
        }
    }
}

@Composable
private fun DevelopmentGlassDialog(
    title: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xD8241A14),
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("此功能正在开发中", color = Color.White.copy(alpha = 0.82f))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("知道了", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(onBackHome: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBackHome) { Text("<", color = Color.White) }
            Text("设置", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text("版本 1.0.0", color = Color.White.copy(alpha = 0.68f))
    }
}

@Composable
private fun SettingsOptionList(
    endpointConfig: CoachEndpointConfig,
    engineSelectionConfig: EngineSelectionConfig,
    onEndpointConfigChange: (CoachEndpointConfig) -> Unit,
    onEngineSelectionChange: (EngineSelectionConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var developmentDialogTitle by remember { mutableStateOf<String?>(null) }

    developmentDialogTitle?.let { title ->
        DevelopmentGlassDialog(
            title = title,
            onDismiss = { developmentDialogTitle = null }
        )
    }

    GlassPanel(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingsOptionRow("语速", "正常（1.0x）", R.drawable.ic_stats) {
                developmentDialogTitle = "语速"
            }
            SettingsOptionRow("音色", "温柔女声", R.drawable.ic_mic) {
                developmentDialogTitle = "音色"
            }
            SettingsOptionRow("提示方式", "智能提示", R.drawable.ic_custom_scene) {
                developmentDialogTitle = "提示方式"
            }
            SettingsOptionRow("识别模式", endpointConfig.title, R.drawable.ic_mic) {
                val next = if (endpointConfig.mode == CoachEndpointMode.UsbDevice) {
                    endpointConfig.useEmulator()
                } else {
                    endpointConfig.useUsbDevice()
                }
                onEndpointConfigChange(next)
            }
            SettingsOptionRow("引擎策略", engineSelectionConfig.profile.title, R.drawable.ic_settings) {
                onEngineSelectionChange(nextEngineSelection(engineSelectionConfig))
            }
            SettingsOptionRow("深浅风格", "深色（温暖）", R.drawable.ic_settings) {
                developmentDialogTitle = "深浅风格"
            }
            SettingsOptionRow("关于", "版本 1.0.0", R.drawable.ic_goal) {
                developmentDialogTitle = "关于"
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(
    title: String,
    value: String,
    @DrawableRes iconRes: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = PracticeColors.Amber,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(value, color = Color.White.copy(alpha = 0.62f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SettingsOptionRow(
    title: String,
    value: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(56.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = PracticeColors.Amber,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(value, color = Color.White.copy(alpha = 0.62f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.62f),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun nextEngineSelection(config: EngineSelectionConfig): EngineSelectionConfig = when (config.profile) {
    EngineProfileMode.StableDemo -> config.useAccuracyFirst()
    EngineProfileMode.AccuracyFirst -> config.useOfflineFirst()
    EngineProfileMode.OfflineFirst -> config.useStableDemo()
}
