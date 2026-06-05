# FastAPI 后端

后端作为增强服务，主链路失败时不影响 Android Demo。

## 本地运行

```bash
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements.txt
.venv/bin/python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

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
- 可选 LangChain / 本地 LLM
- 可选 PaddleSpeech

## 稳定性要求

- 所有接口设置超时。
- 返回结构固定，方便客户端 fallback。
- 不在仓库中保存真实语音、密钥或私有配置。
