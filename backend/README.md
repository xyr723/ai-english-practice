# FastAPI 后端

后端作为增强服务，主链路失败时不影响 Android Demo。

## 本地运行

```bash
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements.txt
.venv/bin/python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

## DeepSeek 大模型教练

后端使用 OpenAI 兼容的 Chat Completions 格式调用 DeepSeek。启动前配置环境变量即可启用，不要把真实 API Key 写入仓库：

```bash
export DEEPSEEK_API_KEY="<your-deepseek-api-key>"
export DEEPSEEK_MODEL=deepseek-v4-flash
export DEEPSEEK_TIMEOUT_SECONDS=8.0
```

可选配置：

```bash
export DEEPSEEK_BASE_URL=https://api.deepseek.com
```

`/coach/analyze` 会优先使用 DeepSeek 生成教练回复、中文翻译、推荐表达和学习建议，并返回 `source=DEEPSEEK`。未配置 key、请求超时或响应格式异常时，接口会自动回退到规则纠错、场景脚本回复和评分链路。

## Android 真机联调

USB 真机推荐使用 `adb reverse`，Android 端默认云端教练地址为 `http://127.0.0.1:8000`：

```bash
adb reverse tcp:8000 tcp:8000
```

启动后端后，在 App 设置页选择 `USB 真机`。练习页生成反馈时会请求 `/coach/analyze`；如果服务不可用，客户端会自动回落到本地分析。

如果使用同一局域网 IP，需要改为：

```bash
.venv/bin/python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

然后在 App 设置页输入 `http://<电脑局域网 IP>:8000`。

## LanguageTool 增强纠错

默认未配置 `LANGUAGETOOL_URL` 时，后端会使用公共 LanguageTool 接口 `https://api.languagetool.org/v2/check`，练习文本会发送到该第三方服务。若需要本地或自建服务，在启动后端前配置检查接口：

```bash
export LANGUAGETOOL_URL=http://127.0.0.1:8081/v2/check
export LANGUAGETOOL_TIMEOUT_SECONDS=4.0
```

LanguageTool 调用成功时返回 `source=LANGUAGE_TOOL`；超时或异常时接口不中断，返回本地规则和场景化建议结果并标记 `source=RULE_FALLBACK`，Android 端会展示对应来源。

## 测试

```bash
PYTHONPATH=. .venv/bin/python -m pytest tests -q
```

## 计划接口

- `POST /grammar/check`
- `POST /coach/analyze`
- `POST /image/generate`
- `POST /asr/paddle`
- `POST /summary`

## 技术选型

- Python 3.11+
- FastAPI
- LanguageTool
- DeepSeek LLM
- 可选 PaddleSpeech

## 稳定性要求

- 所有接口设置超时。
- 返回结构固定，方便客户端 fallback。
- 不在仓库中保存真实语音、密钥或私有配置。
