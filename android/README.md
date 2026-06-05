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
3. 点击 `Start Restaurant Ordering` 或 `Continue` 进入横屏练习页。
4. 点击 `Demo Turn`，模拟用户说出 `I want order a coffee`。
5. 页面展示推荐表达、纠错提示、AI 回复和四维评分。
6. 点击 `Finish` 展示课后总结。

这条链路复用 `PracticeSession`、`RuleCorrectionEngine` 和 `ScoreEngine`，后续 SpeechRecognizer/TTS 接入时不需要重写评分与总结逻辑。

## 页面结构

- `ui.home.HomeScreen`：首页，负责推荐练习和快捷入口。
- `ui.practice.PracticeScreen`：横屏练习页，负责当前文本 fallback 闭环。
- `ui.shared.PlaceholderScreen`：场景、历史、设置的同风格占位页面。
- `ui.theme.PracticeTheme`：颜色和 Material 主题。
- `core.AppNavigator`：轻量路由状态，当前覆盖 Home、Practice、Scenarios、History、Settings。

## 实现顺序

1. 建立最小 Android 工程。
2. 实现文本 fallback 闭环。
3. 实现场景选择页。
4. 接入 SpeechRecognizer 与 TextToSpeech。
5. 接入后端增强能力。
