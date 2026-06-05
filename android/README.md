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

1. App 直接进入点餐练习页。
2. 点击 `Demo Turn`，模拟用户说出 `I want order a coffee`。
3. 页面展示推荐表达、纠错提示、AI 回复和四维评分。
4. 点击 `Finish` 展示课后总结。

这条链路复用 `PracticeSession`、`RuleCorrectionEngine` 和 `ScoreEngine`，后续 SpeechRecognizer/TTS 接入时不需要重写评分与总结逻辑。

## 实现顺序

1. 建立最小 Android 工程。
2. 实现文本 fallback 闭环。
3. 实现场景选择页。
4. 接入 SpeechRecognizer 与 TextToSpeech。
5. 接入后端增强能力。
