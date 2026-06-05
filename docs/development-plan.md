# 开发计划

## 总体原则

- 先完成稳定主链路，再接入增强能力。
- 每个阶段都保留可演示版本。
- 后端和模型能力不可用时，客户端仍能通过 fallback 完成流程。
- 文档、接口和场景脚本随实现同步更新。

## 3 天 Demo 计划

| 时间 | 目标 | 交付内容 |
| --- | --- | --- |
| 2026-06-05 | 完成项目基础与主链路框架 | 场景选择、横屏练习页、TurnBuffer、ASR、TTS、脚本对话 |
| 2026-06-06 | 完成反馈闭环 | 本地纠错、评分系统、课后总结、历史记录、fallback 数据 |
| 2026-06-07 | 完成展示增强与提交材料 | LanguageTool 接入、角色动画、背景图缓存、演示打磨、Demo 视频 |

## 阶段拆分

### 阶段 1：项目初始化

- 建立 Android 与 backend 目录。
- 确认技术栈和最小可运行工程。
- 添加基础场景脚本。
- 建立需求、设计、计划和 UML 文档。

### 阶段 2：Android 主链路

- 实现场景选择页。
- 实现横屏练习页。
- 封装 SpeechRecognizer 和 TextToSpeech。
- 实现 TurnBuffer。
- 使用脚本生成 AI 回复。

### 阶段 3：反馈与评分

- 实现本地纠错规则。
- 实现四维评分。
- 展示原句、推荐表达、评分和提示。
- 实现课后总结页。

### 阶段 4：后端增强

- 搭建 FastAPI 服务。
- 接入 LanguageTool。
- 定义超时和 fallback 策略。
- 预留 LLM、PaddleSpeech 和图像生成接口。

### 阶段 5：演示打磨

- 接入角色 idle/listening/thinking/speaking 状态。
- 准备餐厅场景背景和脚本。
- 优化横屏布局。
- 完成真机演示检查。

## 验证清单

- Android 真机可进入练习页。
- 语音识别能返回文本。
- 松手或停止后能提交完整 Turn。
- TTS 能播放 AI 回复。
- 网络断开时 fallback 可用。
- 评分和纠错结果可解释。
- 课后总结能完整展示。
- README 和文档能说明项目目标、架构和运行方式。

## 实现顺序

1. 后端核心纯规则模块：场景加载、纠错、评分、总结。
2. 后端 FastAPI 接口：`/health`、`/scenarios`、`/grammar/check`、`/coach/analyze`、`/summary`。
3. Android 数据模型和场景选择页。
4. Android 横屏练习页静态 UI。
5. Android 语音主链路：SpeechRecognizer、TurnBuffer、TextToSpeech。
6. Android 纠错/评分展示和课后总结页。
7. 后端增强：LanguageTool、LLM 总结、图像缓存。
8. 演示检查：真机录屏、异常 fallback、README 视频链接。

这个顺序先保证可测试的核心逻辑，再接 UI 和语音能力，避免在 UI 未稳定时混入复杂模型依赖。
