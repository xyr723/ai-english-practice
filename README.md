# AI 英语口语陪练工具

面向真实场景的英语口语训练 Demo，帮助用户在点餐、面试、会议等情境中完成英语对话练习，并获得转写、纠错、评分和课后总结。

## 参赛信息

- 活动批次：2026 年 6 月 5 日 00:00 - 2026 年 6 月 7 日 23:59
- 参赛题目：题目一：AI 英语口语陪练
- 项目定位：Android 端英语口语陪练工具
- 当前阶段：需求、架构与 Demo 方案设计
- 核心目标：用稳定可演示的主链路完成“开口练习 -> 实时反馈 -> 能力提升”的闭环

## 题目要求

开发一款英语口语练习工具，帮助用户在指定场景下进行真实对话训练。

本项目覆盖：

- 场景选择：面试、点餐、会议等。
- 实时语音对话：语音输入、识别、回复与 TTS 播放。
- 发音评测：基于 ASR 置信度和后续可替换的发音评测模块。
- 语法/表达纠错：本地规则与 LanguageTool 增强。
- 课后总结：练习表现、推荐表达、下次目标。
- 量化反馈：语法、流利度、发音清晰度、场景完成度四个维度。

## 核心功能

- 场景选择：预置点餐、面试、会议等场景，支持 JSON 扩展。
- 语音输入：按住说话或点击说话，支持较长句子作为一个 Turn 提交。
- 实时反馈：展示 ASR 转写、轻量纠错、表达建议和多维评分。
- 对话陪练：根据场景脚本和用户输入推进多轮英文对话。
- 课后总结：汇总本次练习表现、常见错误、推荐表达和下次目标。
- 视觉陪练：横屏场景舞台、背景图、2D 角色状态动画。
- 降级可用：后端、网络或增强模型不可用时使用本地 fallback 数据。

## 技术方案

- Android 客户端：Kotlin、Jetpack Compose、SpeechRecognizer、TextToSpeech、Rive/Lottie
- 后端服务：FastAPI、LanguageTool、可选 LangChain/LLM、可选 PaddleSpeech
- 场景数据：JSON 场景脚本、缓存背景图、Demo fallback 数据
- 部署方式：开发演示优先使用 Android 真机 + 本机 FastAPI + Cloudflare Tunnel

后端 Python 依赖见 [backend/requirements.txt](backend/requirements.txt)，Android 依赖见 [android/build.gradle.kts](android/build.gradle.kts)。

## 本地验证

后端测试：

```bash
PYTHONPATH=backend backend/.venv/bin/python -m pytest backend/tests -q
```

Android 单元测试：

```bash
ANDROID_HOME=$HOME/Library/Android/sdk ./gradlew :android:testDebugUnitTest
```

Android Debug 包：

```bash
ANDROID_HOME=$HOME/Library/Android/sdk ./gradlew :android:assembleDebug
```

## 仓库结构

```text
.
├── android/                 # Android 客户端占位与实现说明
├── backend/                 # FastAPI 后端占位与实现说明
│   ├── app/                 # 后端 API 与规则服务
│   ├── tests/               # 后端自动化测试
│   └── requirements.txt     # 后端 Python 依赖
├── docs/
│   ├── requirements.md      # 需求分析
│   ├── architecture.md      # 概要设计与系统架构
│   ├── detailed-design.md   # 详细设计与接口约定
│   ├── development-plan.md  # 开发计划
│   ├── uml.md               # UML/Mermaid 图
│   └── scenarios/           # 场景脚本示例
├── assets/design/           # 设计稿、截图等素材
└── AGENTS.md                # AI 协作开发约束
```

## 文档入口

- [需求分析](docs/requirements.md)
- [概要设计](docs/architecture.md)
- [详细设计](docs/detailed-design.md)
- [开发计划](docs/development-plan.md)
- [UML 图](docs/uml.md)
- [点餐场景脚本示例](docs/scenarios/restaurant.json)

## Demo 范围

第一版 Demo 聚焦稳定主链路：

1. 选择练习场景。
2. 进入横屏练习页。
3. 用户语音输入并展示识别文本。
4. 根据规则库和 LanguageTool 给出纠错建议。
5. 计算语法、流利度、发音清晰度和场景完成度。
6. AI 角色用文本气泡和 TTS 回复。
7. 练习结束后生成课后总结。

增强功能如文生图、PaddleSpeech 备用识别、LLM 个性化总结将作为可选能力接入，不影响主流程演示。

当前 Android Demo 已提供文本 fallback 闭环：点击 `Demo Turn` 模拟一轮点餐输入，页面展示纠错、评分、AI 回复；点击 `Finish` 生成课后总结。语音识别和 TTS 会在后续迭代接入同一状态链路。

Android 入口已补齐首页导航：App 启动进入学习主页，可从首页进入推荐点餐练习、场景入口、历史入口和设置入口。当前场景、历史、设置页保留同风格占位，后续迭代逐步接入真实内容。

## Demo 视频

待补充：提交前将在此处放置可访问的视频链接。
