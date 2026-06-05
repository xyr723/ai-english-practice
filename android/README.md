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
7. 按状态点击 `Start Listening`、`Recognize Demo`、`Ask Coach`、`Show Feedback`，模拟完成一轮英文输入和反馈。
8. 页面展示推荐表达、纠错提示、AI 回复和四维评分。
9. 点击 `Finish` 展示课后总结，点击 `Restart Scene` 可重新开始同一场景。

这条链路复用 `PracticeSession`、`RuleCorrectionEngine` 和 `ScoreEngine`，后续 SpeechRecognizer/TTS 接入时不需要重写评分与总结逻辑。

## 页面结构

- `ui.home.HomeScreen`：首页，负责推荐练习和快捷入口。
- `ui.scenario.ScenarioListScreen`：场景列表页，展示本地场景数据和训练概览。
- `ui.scenario.ScenarioDetailScreen`：场景详情页，展示训练目标、关键词和对话节奏。
- `ui.practice.PracticeScreen`：横屏练习页，负责完整状态展示、文本 fallback、反馈和总结闭环。
- `ui.shared.PlaceholderScreen`：历史、设置的同风格占位页面。
- `ui.theme.PracticeTheme`：颜色和 Material 主题。
- `core.ScenarioCatalog`：本地场景目录，提供推荐场景、列表和按 id 查询。
- `core.PracticeUiState`：练习页状态模型，统一维护状态文案、主操作、时间线、结束和错误恢复标记。
- `core.AppNavigator`：轻量路由状态，当前覆盖 Home、Scenarios、ScenarioDetail、Practice、History、Settings。

## 实现顺序

1. 建立最小 Android 工程。
2. 实现文本 fallback 闭环。
3. 实现场景选择页和详情页。
4. 接入 SpeechRecognizer 与 TextToSpeech。
5. 接入后端增强能力。
