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

## 纠错引擎

### 本地规则

本地规则覆盖 Demo 中的高频问题：

- 缺少礼貌表达：`please`、`could I`、`I'd like to`
- 动词搭配错误：`I want order` -> `I'd like to order`
- 冠词缺失：`order coffee` -> `order a coffee`
- 场景关键词缺失：点餐场景缺少商品或数量

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

## AI 回复生成

优先级：

1. 场景脚本命中回复。
2. 本地 fallback 回复。
3. 后端 LLM 增强回复。

Demo 阶段以脚本和 fallback 为主，保证稳定演示。

## API 约定

### POST /grammar/check

请求：

```json
{
  "text": "I want order a coffee",
  "language": "en-US"
}
```

响应：

```json
{
  "source": "LANGUAGETOOL",
  "matches": [
    {
      "message": "Possible missing word.",
      "offset": 7,
      "length": 5,
      "replacements": ["to order"]
    }
  ]
}
```

### POST /coach/analyze

请求：

```json
{
  "scenarioId": "restaurant",
  "turnText": "I'd like to order a coffee.",
  "history": []
}
```

响应：

```json
{
  "reply": "Sure. Would you like anything to drink with that?",
  "betterExpression": "I'd like to order a coffee, please.",
  "tips": ["Use 'please' to sound more polite."]
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

