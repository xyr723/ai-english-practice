package com.xengineer.aienglishpractice.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xengineer.aienglishpractice.core.CoachEndpointConfig
import com.xengineer.aienglishpractice.core.CoachEndpointMode
import com.xengineer.aienglishpractice.core.EngineProfileMode
import com.xengineer.aienglishpractice.core.EngineSelectionConfig
import com.xengineer.aienglishpractice.ui.shared.DarkPanel
import com.xengineer.aienglishpractice.ui.shared.LightPanel
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
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SettingsHeader(onBackHome = onBackHome)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                DarkPanel(modifier = Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("当前云端教练地址", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
                        Text(
                            text = endpointConfig.title,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(endpointConfig.baseUrl, color = Color(0xFFEAD7C4))
                        Text(endpointConfig.setupHint, color = Color(0xFFEAD7C4))
                        Spacer(Modifier.height(6.dp))
                        Text("引擎策略", color = PracticeColors.Amber, fontWeight = FontWeight.Bold)
                        Text(
                            text = engineSelectionConfig.profile.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(engineSelectionConfig.profile.description, color = Color(0xFFEAD7C4))
                        engineSelectionConfig.engineSummaries.forEach { summary ->
                            Text(summary, color = Color(0xFFEAD7C4))
                        }
                    }
                }
                LightPanel(modifier = Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("连接方式", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
                        EndpointAction(
                            text = "USB 真机",
                            selected = endpointConfig.mode == CoachEndpointMode.UsbDevice,
                            onClick = { onEndpointConfigChange(endpointConfig.useUsbDevice()) }
                        )
                        EndpointAction(
                            text = "Android 模拟器",
                            selected = endpointConfig.mode == CoachEndpointMode.Emulator,
                            onClick = { onEndpointConfigChange(endpointConfig.useEmulator()) }
                        )
                        OutlinedTextField(
                            value = customInput,
                            onValueChange = { customInput = it },
                            label = { Text("自定义 URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        PrimaryAction(
                            text = "使用自定义地址",
                            onClick = { onEndpointConfigChange(endpointConfig.useCustom(customInput)) }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("引擎模式", color = PracticeColors.Ink, fontWeight = FontWeight.Bold)
                        EngineProfileAction(
                            text = EngineProfileMode.StableDemo.title,
                            selected = engineSelectionConfig.profile == EngineProfileMode.StableDemo,
                            onClick = { onEngineSelectionChange(engineSelectionConfig.useStableDemo()) }
                        )
                        EngineProfileAction(
                            text = EngineProfileMode.AccuracyFirst.title,
                            selected = engineSelectionConfig.profile == EngineProfileMode.AccuracyFirst,
                            onClick = { onEngineSelectionChange(engineSelectionConfig.useAccuracyFirst()) }
                        )
                        EngineProfileAction(
                            text = EngineProfileMode.OfflineFirst.title,
                            selected = engineSelectionConfig.profile == EngineProfileMode.OfflineFirst,
                            onClick = { onEngineSelectionChange(engineSelectionConfig.useOfflineFirst()) }
                        )
                    }
                }
            }
            Text(
                text = "默认推荐 USB 真机方式，适合当前 adb 连接设备。",
                color = Color(0xFFDCEDEA)
            )
        }
    }
}

@Composable
private fun SettingsHeader(onBackHome: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "设置",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text("云端教练连接", color = Color(0xFFDCEDEA), style = MaterialTheme.typography.titleMedium)
        }
        TextButton(onClick = onBackHome) {
            Text("首页", color = Color.White)
        }
    }
}

@Composable
private fun EndpointAction(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        PrimaryAction(text = "$text · 当前", onClick = onClick)
    } else {
        TextButton(onClick = onClick) {
            Text(text, color = PracticeColors.Ink)
        }
    }
    Spacer(Modifier.height(2.dp))
}

@Composable
private fun EngineProfileAction(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        PrimaryAction(text = "$text · 当前", onClick = onClick)
    } else {
        TextButton(onClick = onClick) {
            Text(text, color = PracticeColors.Ink)
        }
    }
}
