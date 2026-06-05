# FastAPI 后端

后端作为增强服务，主链路失败时不影响 Android Demo。

## 本地运行

```bash
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements.txt
.venv/bin/python -m uvicorn app.main:app --reload
```

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
