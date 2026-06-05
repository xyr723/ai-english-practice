# 详细设计

## 核心状态模型

```kotlin
enum class PracticeState {
    Idle,
    Listening,
    Recognizing,
    Thinking,
    Speaking,
    Finished,
    Error
}
```

状态含义：

- `Idle`：等待用户操作。
- `Listening`：用户正在说话。
- `Recognizing`：识别结果处理中。
- `Thinking`：生成纠错、评分或回复。
- `Speaking`：AI 角色正在回复。
- `Finished`：本次练习结束。
- `Error`：出现可恢复异常。

状态切换由明确事件驱动，UI 不直接修改状态：

| 事件 | 来源 | 目标状态 |
| --- | --- | --- |
| `StartPractice` | 用户选择场景 | `Idle` |
| `StartListening` | 用户按下麦克风 | `Listening` |
| `StopListening` | 用户松开或点击停止 | `Recognizing` |
| `SubmitTurn` | ASR 返回最终文本 | `Thinking` |
| `ReplyReady` | 纠错、评分、回复完成 | `Speaking` |
| `TtsFinished` | TTS 播放结束 | `Idle` |
| `FinishPractice` | 用户结束练习 | `Finished` |
| `RecoverableError` | ASR 或后端失败 | `Error` |
| `Retry` | 用户重试 | `Idle` |

## 场景脚本

场景脚本使用 JSON 描述：

- `id`：场景唯一标识。
- `name`：场景名称。
- `role`：AI 扮演角色。
- `opening`：开场白。
- `goals`：用户需要完成的任务。
- `keywords`：场景关键词。
- `turns`：多轮脚本和匹配规则。
- `fallbackReplies`：无法匹配时的回复。

示例见 [restaurant.json](scenarios/restaurant.json)。

场景脚本校验规则：

- `id`、`name`、`role`、`opening` 必填。
- `goals` 至少包含 1 个目标。
- `turns` 至少包含 1 轮。
- 每个 turn 必须包含 `id`、`expectedIntent` 和 `reply`。
- `fallbackReplies` 至少包含 1 条。

## TurnBuffer

职责：

- 收集 ASR partial text。
- 去重或合并重复片段。
- 在用户松手、点击停止或超时后提交 final text。
- 记录单轮开始时间、结束时间、词数和置信度。

输出结构：

```json
{
  "turnId": "turn-001",
  "text": "I'd like to order a coffee and a sandwich, please.",
  "durationMs": 8200,
  "wordCount": 10,
  "asrConfidence": 0.86
}
```

边界规则：

- partial text 为空时不提交。
- final text 优先级高于 partial text。
- 如果 ASR 连续返回重复 partial text，仅保留最新稳定结果。
- 如果本轮超过 30 秒，UI 提示用户结束本轮；后端不依赖这个限制。

## 纠错引擎

### 本地规则

本地规则覆盖 Demo 中的高频问题：

- 缺少礼貌表达：`please`、`could I`、`I'd like to`
- 动词搭配错误：`I want order` -> `I'd like to order`
- 冠词缺失：`order coffee` -> `order a coffee`
- 场景关键词缺失：点餐场景缺少商品或数量

输出结构：

```json
{
  "original": "I want order a coffee",
  "betterExpression": "I'd like to order a coffee, please.",
  "issues": [
    {
      "type": "grammar",
      "message": "Use 'to order' after 'want'.",
      "suggestion": "I'd like to order"
    },
    {
      "type": "politeness",
      "message": "Add a polite expression in ordering scenes.",
      "suggestion": "please"
    }
  ],
  "source": "RULE_ONLY"
}
```

### LanguageTool 增强

后端返回语法检查结果，客户端只展示高置信度建议。若超时或失败，客户端保留本地规则结果。

## 评分公式

总分不直接给出单一黑盒分数，而是展示四个维度。

### 语法准确度

```text
grammarScore = clamp(100 - grammarErrorCount * 8 - spellingErrorCount * 4, 0, 100)
```

### 流利度

```text
wpm = wordCount / durationMinutes
fluencyScore = scoreByRange(wpm, targetRange = 80..140)
```

### 发音清晰度

```text
pronunciationScore = round(asrConfidence * 100)
```

Demo 阶段使用 ASR confidence 近似表示，后续可替换为专业发音评测。

### 场景完成度

```text
completionScore = matchedGoals / totalGoals * 100
```

### 评分输出

```json
{
  "grammar": {
    "score": 82,
    "reason": "Found 2 grammar or expression issues."
  },
  "fluency": {
    "score": 76,
    "reason": "Speaking speed is slightly below the target range."
  },
  "pronunciation": {
    "score": 80,
    "reason": "ASR confidence is 0.80."
  },
  "completion": {
    "score": 70,
    "reason": "Matched 2 of 3 scene goals."
  }
}
```

## AI 回复生成

优先级：

1. 场景脚本命中回复。
2. 本地 fallback 回复。
3. 后端 LLM 增强回复。

Demo 阶段以脚本和 fallback 为主，保证稳定演示。

回复生成不直接依赖 LLM。LLM 只作为增强器，用来改写表达或丰富总结；当 LLM 失败时，脚本回复仍然可用。

## 异常处理

| 异常 | 处理方式 | 用户感知 |
| --- | --- | --- |
| ASR 无结果 | 保持当前轮次，允许重试 | “没有识别到内容，请再说一次” |
| ASR 权限缺失 | 引导开启麦克风权限 | “需要麦克风权限才能练习” |
| 后端超时 | 使用本地纠错和 fallback 回复 | 不打断练习 |
| 场景脚本缺失 | 返回默认点餐场景或提示配置错误 | “场景加载失败” |
| TTS 播放失败 | 展示文本回复，跳过语音播放 | “语音播放失败，已显示文字” |

## API 约定

### GET /health

响应：

```json
{
  "status": "ok"
}
```

### GET /scenarios

响应：

```json
{
  "items": [
    {
      "id": "restaurant",
      "name": "Restaurant Ordering",
      "role": "Waitress",
      "level": "A2-B1"
    }
  ]
}
```

### POST /grammar/check

请求：

```json
{
  "text": "I want order a coffee",
  "language": "en-US",
  "scenarioId": "restaurant"
}
```

响应：

```json
{
  "original": "I want order a coffee",
  "betterExpression": "I'd like to order a coffee, please.",
  "issues": [
    {
      "type": "grammar",
      "message": "Use 'to order' or a polite ordering expression.",
      "suggestion": "I'd like to order"
    }
  ],
  "source": "RULE_ONLY"
}
```

### POST /coach/analyze

请求：

```json
{
  "scenarioId": "restaurant",
  "turnText": "I want order a coffee",
  "history": [],
  "durationMs": 6000,
  "asrConfidence": 0.8,
  "turnIndex": 0
}
```

响应：

```json
{
  "reply": "Sure. Would you like anything to drink with that?",
  "betterExpression": "I'd like to order a coffee, please.",
  "tips": ["Use 'please' to sound more polite."],
  "scores": {
    "grammar": {
      "score": 84,
      "reason": "Found 2 grammar or spelling issues."
    },
    "fluency": {
      "score": 100,
      "reason": "Speaking speed is 100 words per minute."
    },
    "pronunciation": {
      "score": 80,
      "reason": "ASR confidence is 0.80."
    },
    "completion": {
      "score": 33,
      "reason": "Matched 1 of 3 scene goals."
    }
  },
  "source": "RULE_ONLY"
}
```

### POST /summary

请求：

```json
{
  "scenarioId": "restaurant",
  "turns": [
    {
      "userText": "I want order a coffee",
      "correctedText": "I'd like to order a coffee, please.",
      "scores": {
        "grammar": 82,
        "fluency": 76,
        "pronunciation": 80,
        "completion": 70
      }
    }
  ]
}
```

响应：

```json
{
  "averageScore": 77,
  "strengths": ["You completed the ordering goal."],
  "improvements": ["Use polite ordering expressions more often."],
  "nextGoal": "Practice confirming price and takeaway options."
}
```

## 后端代码结构

```text
backend/
├── pyproject.toml
├── app/
│   ├── main.py
│   ├── schemas.py
│   └── services/
│       ├── correction.py
│       ├── scoring.py
│       ├── scenarios.py
│       └── summary.py
└── tests/
    ├── test_correction.py
    ├── test_scoring.py
    └── test_api.py
```
