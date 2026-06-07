from fastapi.testclient import TestClient

import app.main as main_module
from app.main import app


client = TestClient(app)


def setup_function():
    main_module.LANGUAGE_TOOL_CHECKER = None


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
    assert body["scores"]["pronunciation"]["score"] == 80
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
