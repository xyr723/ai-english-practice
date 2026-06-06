# Android 客户端

客户端计划使用 Kotlin + Jetpack Compose 实现。

## 环境要求

- JDK 17 或更高版本。
- Android SDK，当前工程使用 `compileSdk 35`。
- 不需要全局安装 Gradle，仓库已配置 Gradle Wrapper。

macOS 示例：

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :android:testDebugUnitTest
./gradlew :android:assembleDebug
```

Debug APK 输出位置：

```text
android/build/outputs/apk/debug/android-debug.apk
```

## 主要模块

- 场景选择页
- 横屏练习页
- 课后总结页
- `SpeechRecognizerAdapter`
- `TextToSpeechAdapter`
- `TurnBuffer`
- `ScenarioEngine`
- `RuleCorrectionEngine`
- `ScoreEngine`
- `DemoFallbackRepository`

## 当前闭环

当前版本先实现文本 fallback 闭环：

1. App 进入同风格学习主页。
2. 首页展示今日目标、推荐点餐场景、快速入口和继续练习按钮。
3. 点击 `Scenarios` 进入本地场景列表，当前包含点餐、面试、会议三个场景。
4. 点击场景卡片进入详情页，查看角色、等级、目标、关键词和对话节奏。
5. 点击 `Start Practice` 进入对应场景的横屏练习页。
6. 练习页展示 `Ready`、`Listening`、`Recognized transcript`、`Coach is checking`、`Feedback ready`、`Session complete` 和错误恢复状态。
7. 点击 `Start Speech` 请求麦克风权限并启动 Android SpeechRecognizer；识别结果进入 transcript 区域。
8. 没有权限或设备不支持识别时，页面保留 demo fallback，可继续用 `Recognize Demo` 完成演示链路。
9. 点击 `生成反馈` 后，默认请求云端教练 `/coach/analyze`，成功时使用云端回复、纠错和评分。
10. 云端不可用时自动回落到本地分析，保证演示闭环不断。
11. 页面展示推荐表达、纠错提示、AI 回复、四维评分和反馈来源。
12. TTS 默认开启，`朗读回复` 可朗读教练开场或回复，也可以通过 `关闭朗读` 关闭。
13. 点击 `完成` 展示课后总结，并把本次总结写入本地练习记录。
14. 点击 `记录` 查看本地练习记录、最近分数、下一步目标，并可重复练习同一场景或清空记录。
15. 回到首页后，今日完成 turn 数和最近一次总结会从本地历史更新。

Android 真机默认通过 USB bridge 访问 `http://127.0.0.1:8000`，对应本机运行的 FastAPI 服务。后端不可用时仍可通过本地 fallback 完成练习、总结和历史记录。

## 云端教练地址

设置页提供三种地址：

- `USB 真机`：默认地址 `http://127.0.0.1:8000`，需要先执行 `adb reverse tcp:8000 tcp:8000`。
- `Android 模拟器`：地址 `http://10.0.2.2:8000`，用于模拟器访问宿主机。
- `自定义地址`：用于局域网 IP 或隧道地址，例如 `http://192.168.1.23:8000`。

真机 USB 联调步骤：

```bash
cd backend
. .venv/bin/activate
PYTHONPATH=. uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
adb reverse tcp:8000 tcp:8000
```

安装 Debug APK 后，进入 `设置` 确认连接方式为 `USB 真机`，再进入练习页生成反馈。若页面显示 `来源：云端教练`，说明真机到 FastAPI 的闭环已接通。

## 页面结构

- `ui.home.HomeScreen`：首页，负责推荐练习和快捷入口。
- `ui.scenario.ScenarioListScreen`：场景列表页，展示本地场景数据和训练概览。
- `ui.scenario.ScenarioDetailScreen`：场景详情页，展示训练目标、关键词和对话节奏。
- `ui.practice.PracticeScreen`：横屏练习页，负责完整状态展示、文本 fallback、反馈和总结闭环。
- `ui.history.HistoryScreen`：本地历史页，展示完成记录、累计 turn、平均分和重复练习入口。
- `ui.settings.CoachSettingsScreen`：设置页，支持云端教练地址配置和多引擎策略预留入口。
- `ui.theme.PracticeTheme`：颜色和 Material 主题。
- `core.ScenarioCatalog`：本地场景目录，提供推荐场景、列表和按 id 查询。
- `core.PracticeUiState`：练习页状态模型，统一维护状态文案、主操作、时间线、结束和错误恢复标记。
- `core.VoiceUiState`：语音输入/TTS 状态模型，维护 speech/demo 模式、权限、识别文本、错误和 TTS 开关。
- `core.CoachEndpointConfig`：云端教练地址配置，维护 USB 真机、模拟器和自定义地址。
- `core.EngineSelectionConfig`：ASR、TTS 和判定引擎策略预留接口，维护稳定演示、效果优先和离线优先模式。
- `core.CoachBackendUiState`：云端/fallback 状态模型，维护 Auto、云端、本地和反馈来源。
- `core.PracticeHistoryStore`：进程内本地历史仓库，负责记录完成摘要、最近记录、累计 turn 和清空记录。
- `core.AppNavigator`：轻量路由状态，当前覆盖 Home、Scenarios、ScenarioDetail、Practice、History、Settings。
- `network.CoachApiClient`：后端 `/coach/analyze` 客户端，负责请求 JSON 和映射 `TurnResult`。
- `voice.SpeechRecognizerAdapter`：Android SpeechRecognizer 封装，提供 ready、partial、final 和 error 回调。
- `voice.TextToSpeechAdapter`：Android TextToSpeech 封装，负责教练文本朗读和生命周期释放。

## 实现顺序

1. 建立最小 Android 工程。
2. 实现文本 fallback 闭环。
3. 实现场景选择页和详情页。
4. 接入 SpeechRecognizer 与 TextToSpeech。
5. 接入后端增强能力。
