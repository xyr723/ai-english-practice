from fastapi.testclient import TestClient

import app.main as main_module
from app.main import app


client = TestClient(app)


def setup_function():
    main_module.LANGUAGE_TOOL_CHECKER = None
    main_module.DEEPSEEK_COACH_CLIENT = None


def test_health_endpoint():
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_coach_analyze_returns_reply_correction_and_scores():
    response = client.post(
        "/coach/analyze",
        json={
            "scenarioId": "restaurant",
            "turnText": "I want order a coffee",
            "history": [],
            "durationMs": 6000,
            "asrConfidence": 0.8,
            "turnIndex": 0,
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["reply"] == "Sure. Would you like anything to drink?"
    assert body["replyTranslation"] == "当然。你想喝点什么吗？"
    assert body["betterExpression"] == "I'd like to order a coffee, please."
    assert body["scores"]["grammar"]["score"] == 84
    assert body["scores"]["pronunciation"]["score"] == 80
    assert body["source"] == "RULE_ONLY"


def test_coach_analyze_uses_deepseek_client_when_available():
    class FakeDeepSeekCoachClient:
        def analyze(self, scenario, request, fallback):
            assert scenario["id"] == "restaurant"
            assert request.turnText == "I want order a coffee"
            assert fallback["reply"] == "Sure. Would you like anything to drink?"
            return {
                "reply": "Sure. What size coffee would you like?",
                "replyTranslation": "当然。您想要多大杯的咖啡？",
                "betterExpression": "I'd like to order a coffee, please.",
                "tips": ["表达更自然，可以补充杯型。"],
            }

    main_module.DEEPSEEK_COACH_CLIENT = FakeDeepSeekCoachClient()

    response = client.post(
        "/coach/analyze",
        json={
            "scenarioId": "restaurant",
            "turnText": "I want order a coffee",
            "history": [],
            "durationMs": 6000,
            "asrConfidence": 0.8,
            "turnIndex": 0,
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["reply"] == "Sure. What size coffee would you like?"
    assert body["replyTranslation"] == "当然。您想要多大杯的咖啡？"
    assert body["betterExpression"] == "I'd like to order a coffee, please."
    assert body["tips"] == ["表达更自然，可以补充杯型。"]
    assert body["scores"]["grammar"]["score"] == 84
    assert body["source"] == "DEEPSEEK"


def test_coach_analyze_falls_back_to_rules_when_deepseek_fails():
    class FailingDeepSeekCoachClient:
        def analyze(self, scenario, request, fallback):
            raise RuntimeError("DeepSeek request failed")

    main_module.DEEPSEEK_COACH_CLIENT = FailingDeepSeekCoachClient()

    response = client.post(
        "/coach/analyze",
        json={
            "scenarioId": "restaurant",
            "turnText": "I want order a coffee",
            "history": [],
            "durationMs": 6000,
            "asrConfidence": 0.8,
            "turnIndex": 0,
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["reply"] == "Sure. Would you like anything to drink?"
    assert body["betterExpression"] == "I'd like to order a coffee, please."
    assert body["source"] == "RULE_ONLY"


def test_coach_analyze_skips_drink_question_when_food_and_drink_are_present():
    response = client.post(
        "/coach/analyze",
        json={
            "scenarioId": "restaurant",
            "turnText": "I want a cup of tea and a hamburger",
            "history": [],
            "durationMs": 6200,
            "asrConfidence": 0.86,
            "turnIndex": 0,
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["reply"] == "Great. Is that for here or takeaway?"
    assert body["replyTranslation"] == "好的。是在这里吃还是外带？"


def test_coach_analyze_passes_language_tool_source(monkeypatch):
    def fake_checker(text, language):
        return [
            {
                "message": "Possible agreement error.",
                "offset": 2,
                "length": 3,
                "replacements": [{"value": "have"}],
                "rule": {"category": {"id": "GRAMMAR"}},
            }
        ]

    monkeypatch.setattr(main_module, "LANGUAGE_TOOL_CHECKER", fake_checker)

    response = client.post(
        "/coach/analyze",
        json={
            "scenarioId": "restaurant",
            "turnText": "I has coffee",
            "history": [],
            "durationMs": 6000,
            "asrConfidence": 0.8,
            "turnIndex": 0,
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["betterExpression"] == "I have coffee, please."
    assert body["source"] == "LANGUAGE_TOOL"


def test_coach_analyze_accepts_android_builtin_scene_without_json_file():
    response = client.post(
        "/coach/analyze",
        json={
            "scenarioId": "interview",
            "turnText": "My name is Alex and I built a project with my team",
            "history": [],
            "durationMs": 7000,
            "asrConfidence": 0.85,
            "turnIndex": 0,
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["reply"] == "Good. Could you tell me about one project you worked on?"
    assert body["source"] == "RULE_ONLY"


def test_coach_analyze_accepts_android_custom_scene_without_json_file():
    response = client.post(
        "/coach/analyze",
        json={
            "scenarioId": "custom-airport-机场改签",
            "turnText": "I need to change my flight, please",
            "history": [],
            "durationMs": 7000,
            "asrConfidence": 0.85,
            "turnIndex": 0,
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["reply"] == "Sure. Which flight would you like to change?"
    assert "flight number" in body["betterExpression"]
    assert any("flight number" in tip for tip in body["tips"])


def test_coach_analyze_accepts_unknown_custom_scene_without_json_file():
    response = client.post(
        "/coach/analyze",
        json={
            "scenarioId": "custom-custom-健身房办卡",
            "turnText": "I need to buy a gym membership, please",
            "history": [],
            "durationMs": 7000,
            "asrConfidence": 0.85,
            "turnIndex": 0,
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert "健身房办卡" in body["reply"]


def test_coach_analyze_returns_scenario_guidance_for_interview():
    response = client.post(
        "/coach/analyze",
        json={
            "scenarioId": "interview",
            "turnText": "I worked on a project",
            "history": [],
            "durationMs": 7000,
            "asrConfidence": 0.85,
            "turnIndex": 0,
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert "STAR" in body["betterExpression"]
    assert any("STAR" in tip for tip in body["tips"])


def test_summary_endpoint_aggregates_nested_scores_and_turn_reviews():
    response = client.post(
        "/summary",
        json={
            "scenarioId": "restaurant",
            "turns": [
                {
                    "userText": "I want order a coffee",
                    "betterExpression": "I'd like to order a coffee, please.",
                    "reply": "Sure. Would you like anything to drink?",
                    "scores": {
                        "grammar": {"score": 84, "reason": "Found 2 grammar or spelling issues."},
                        "fluency": {"score": 100, "reason": "Speaking speed is 100 words per minute."},
                        "pronunciation": {"score": 80, "reason": "ASR confidence is 0.80."},
                        "completion": {"score": 67, "reason": "Matched 2 of 3 scene goals."},
                    },
                    "tips": ["Add a polite expression in ordering scenes."],
                }
            ],
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["source"] == "RULE_ONLY"
    assert body["turnCount"] == 1
    assert body["averageScore"] == 83
    assert body["scoreBreakdown"][0] == {
        "label": "语法",
        "score": 84,
        "reason": "Found 2 grammar or spelling issues.",
    }
    assert body["turnReviews"][0]["userText"] == "I want order a coffee"
    assert body["turnReviews"][0]["betterExpression"] == "I'd like to order a coffee, please."
    assert body["practicePlan"]


def test_summary_uses_deepseek_client_when_available():
    class FakeDeepSeekCoachClient:
        def summarize(self, scenario, request, fallback):
            assert scenario["id"] == "restaurant"
            assert request.scenarioId == "restaurant"
            assert fallback["averageScore"] == 83
            return {
                "averageScore": 91,
                "strengths": ["表达目标清楚，能完成点餐。"],
                "improvements": ["下一轮补充杯型和堂食/外带。"],
                "nextGoal": "用一句话同时说明饮品、杯型和外带选项。",
                "practicePlan": ["朗读优化表达 3 遍。"],
            }

    main_module.DEEPSEEK_COACH_CLIENT = FakeDeepSeekCoachClient()

    response = client.post(
        "/summary",
        json={
            "scenarioId": "restaurant",
            "turns": [
                {
                    "userText": "I want order a coffee",
                    "betterExpression": "I'd like to order a coffee, please.",
                    "reply": "Sure. Would you like anything to drink?",
                    "scores": {
                        "grammar": {"score": 84, "reason": "Found 2 grammar or spelling issues."},
                        "fluency": {"score": 100, "reason": "Speaking speed is 100 words per minute."},
                        "pronunciation": {"score": 80, "reason": "ASR confidence is 0.80."},
                        "completion": {"score": 67, "reason": "Matched 2 of 3 scene goals."},
                    },
                    "tips": ["Add a polite expression in ordering scenes."],
                }
            ],
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["source"] == "DEEPSEEK"
    assert body["averageScore"] == 91
    assert body["strengths"] == ["表达目标清楚，能完成点餐。"]
    assert body["practicePlan"] == ["朗读优化表达 3 遍。"]
    assert body["turnReviews"][0]["userText"] == "I want order a coffee"


def test_summary_falls_back_to_rules_when_deepseek_fails():
    class FailingDeepSeekCoachClient:
        def summarize(self, scenario, request, fallback):
            raise RuntimeError("DeepSeek request failed")

    main_module.DEEPSEEK_COACH_CLIENT = FailingDeepSeekCoachClient()

    response = client.post(
        "/summary",
        json={
            "scenarioId": "restaurant",
            "turns": [
                {
                    "userText": "I want order a coffee",
                    "betterExpression": "I'd like to order a coffee, please.",
                    "reply": "Sure. Would you like anything to drink?",
                    "scores": {
                        "grammar": {"score": 84, "reason": "Found 2 grammar or spelling issues."},
                        "fluency": {"score": 100, "reason": "Speaking speed is 100 words per minute."},
                        "pronunciation": {"score": 80, "reason": "ASR confidence is 0.80."},
                        "completion": {"score": 67, "reason": "Matched 2 of 3 scene goals."},
                    },
                    "tips": ["Add a polite expression in ordering scenes."],
                }
            ],
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["source"] == "RULE_FALLBACK"
    assert body["averageScore"] == 83
    assert body["nextGoal"]
