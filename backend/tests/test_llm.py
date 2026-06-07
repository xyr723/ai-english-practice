from types import SimpleNamespace

from app.services.llm import (
    DEFAULT_DEEPSEEK_BASE_URL,
    DEFAULT_DEEPSEEK_MODEL,
    DeepSeekCoachClient,
    create_deepseek_coach_client,
)


def test_create_deepseek_coach_client_requires_api_key(monkeypatch):
    monkeypatch.delenv("DEEPSEEK_API_KEY", raising=False)

    client = create_deepseek_coach_client()

    assert client is None


def test_create_deepseek_coach_client_uses_flash_model_by_default(monkeypatch):
    monkeypatch.setenv("DEEPSEEK_API_KEY", "test-key")
    monkeypatch.delenv("DEEPSEEK_MODEL", raising=False)
    monkeypatch.delenv("DEEPSEEK_BASE_URL", raising=False)

    client = create_deepseek_coach_client()

    assert isinstance(client, DeepSeekCoachClient)
    assert client.model == DEFAULT_DEEPSEEK_MODEL
    assert client.base_url == DEFAULT_DEEPSEEK_BASE_URL


def test_deepseek_client_posts_openai_compatible_chat_completion(monkeypatch):
    captured = {}

    class FakeResponse:
        def raise_for_status(self):
            return None

        def json(self):
            return {
                "choices": [
                    {
                        "message": {
                            "content": (
                                '{"reply":"Sure. What size coffee would you like?",'
                                '"replyTranslation":"当然。您想要多大杯的咖啡？",'
                                '"betterExpression":"I would like a coffee, please.",'
                                '"tips":["补充杯型会更自然。"]}'
                            )
                        }
                    }
                ]
            }

    def fake_post(url, headers, json, timeout):
        captured["url"] = url
        captured["headers"] = headers
        captured["json"] = json
        captured["timeout"] = timeout
        return FakeResponse()

    monkeypatch.setattr("app.services.llm.httpx.post", fake_post)
    client = DeepSeekCoachClient(api_key="test-key", timeout_seconds=3.5)

    result = client.analyze(
        scenario={
            "id": "restaurant",
            "name": "餐厅点餐",
            "role": "服务员",
            "goals": ["order_food_or_drink"],
            "keywords": ["coffee"],
            "opening": "Welcome!",
        },
        request=SimpleNamespace(
            turnText="I want order coffee",
            history=[],
            durationMs=6000,
            asrConfidence=0.8,
            turnIndex=0,
        ),
        fallback={
            "reply": "Sure. Would you like anything to drink?",
            "replyTranslation": "当然。你想喝点什么吗？",
            "betterExpression": "I'd like to order coffee, please.",
            "tips": ["Use a polite expression."],
        },
    )

    assert captured["url"] == "https://api.deepseek.com/chat/completions"
    assert captured["headers"]["Authorization"] == "Bearer test-key"
    assert captured["json"]["model"] == DEFAULT_DEEPSEEK_MODEL
    assert captured["json"]["response_format"] == {"type": "json_object"}
    assert captured["json"]["stream"] is False
    assert captured["timeout"] == 3.5
    assert result["reply"] == "Sure. What size coffee would you like?"
    assert result["replyTranslation"] == "当然。您想要多大杯的咖啡？"
    assert result["betterExpression"] == "I would like a coffee, please."
    assert result["tips"] == ["补充杯型会更自然。"]


def test_deepseek_client_can_override_valid_scores_and_fallback_invalid_dimensions(monkeypatch):
    class FakeResponse:
        def raise_for_status(self):
            return None

        def json(self):
            return {
                "choices": [
                    {
                        "message": {
                            "content": (
                                '{"reply":"Sure. What size coffee would you like?",'
                                '"replyTranslation":"当然。您想要多大杯的咖啡？",'
                                '"betterExpression":"I would like a coffee, please.",'
                                '"tips":["补充杯型会更自然。"],'
                                '"scores":{'
                                '"fluency":{"score":72,"reason":"真实录音语速偏慢。"},'
                                '"pronunciation":{"score":105,"reason":"超过范围，应回退。"}'
                                "}}"
                            )
                        }
                    }
                ]
            }

    monkeypatch.setattr("app.services.llm.httpx.post", lambda *args, **kwargs: FakeResponse())
    client = DeepSeekCoachClient(api_key="test-key")

    result = client.analyze(
        scenario={
            "id": "restaurant",
            "name": "餐厅点餐",
            "role": "服务员",
            "goals": ["order_food_or_drink"],
            "keywords": ["coffee"],
            "opening": "Welcome!",
        },
        request=SimpleNamespace(
            turnText="I want order coffee",
            history=[],
            durationMs=12000,
            asrConfidence=0.62,
            turnIndex=0,
        ),
        fallback={
            "reply": "Sure. Would you like anything to drink?",
            "replyTranslation": "当然。你想喝点什么吗？",
            "betterExpression": "I'd like to order coffee, please.",
            "tips": ["Use a polite expression."],
            "scores": {
                "grammar": {"score": 84, "reason": "Found 2 issues."},
                "fluency": {"score": 100, "reason": "Speaking speed is 100 words per minute."},
                "pronunciation": {"score": 62, "reason": "ASR confidence is 0.62."},
                "completion": {"score": 67, "reason": "Matched 2 of 3 scene goals."},
            },
        },
    )

    assert result["scores"]["grammar"]["score"] == 84
    assert result["scores"]["fluency"] == {"score": 72, "reason": "真实录音语速偏慢。"}
    assert result["scores"]["pronunciation"] == {
        "score": 62,
        "reason": "ASR confidence is 0.62.",
    }
    assert result["scores"]["completion"]["score"] == 67


def test_deepseek_client_summarizes_practice_with_fixed_json_shape(monkeypatch):
    captured = {}

    class FakeResponse:
        def raise_for_status(self):
            return None

        def json(self):
            return {
                "choices": [
                    {
                        "message": {
                            "content": (
                                '{"averageScore":91,'
                                '"strengths":["表达目标清楚。"],'
                                '"improvements":["补充杯型和外带选项。"],'
                                '"nextGoal":"用一句话完成完整点餐。",'
                                '"practicePlan":["朗读优化表达 3 遍。"]}'
                            )
                        }
                    }
                ]
            }

    def fake_post(url, headers, json, timeout):
        captured["url"] = url
        captured["headers"] = headers
        captured["json"] = json
        captured["timeout"] = timeout
        return FakeResponse()

    monkeypatch.setattr("app.services.llm.httpx.post", fake_post)
    client = DeepSeekCoachClient(api_key="test-key", timeout_seconds=3.5)

    result = client.summarize(
        scenario={
            "id": "restaurant",
            "name": "餐厅点餐",
            "role": "服务员",
            "goals": ["order_food_or_drink"],
            "keywords": ["coffee"],
            "opening": "Welcome!",
        },
        request=SimpleNamespace(
            scenarioId="restaurant",
            turns=[
                {
                    "userText": "I want order coffee",
                    "betterExpression": "I'd like to order coffee, please.",
                    "scores": {
                        "grammar": {"score": 84, "reason": "Found 2 issues."},
                        "fluency": {"score": 100, "reason": "Good speed."},
                    },
                }
            ],
        ),
        fallback={
            "turnCount": 1,
            "averageScore": 83,
            "strengths": ["You completed the practice flow."],
            "improvements": ["Review corrected expressions."],
            "nextGoal": "Practice confirming takeaway options.",
            "practicePlan": ["Repeat the scene."],
            "scoreBreakdown": [],
            "turnReviews": [],
        },
    )

    assert captured["url"] == "https://api.deepseek.com/chat/completions"
    assert captured["headers"]["Authorization"] == "Bearer test-key"
    assert captured["json"]["response_format"] == {"type": "json_object"}
    assert "practice summary" in captured["json"]["messages"][0]["content"].lower()
    assert result["averageScore"] == 91
    assert result["strengths"] == ["表达目标清楚。"]
    assert result["improvements"] == ["补充杯型和外带选项。"]
    assert result["nextGoal"] == "用一句话完成完整点餐。"
    assert result["practicePlan"] == ["朗读优化表达 3 遍。"]
    assert result["turnCount"] == 1
