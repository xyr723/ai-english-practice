from fastapi.testclient import TestClient

import app.main as main_module
from app.main import app


client = TestClient(app)


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
    assert body["betterExpression"] == "I'd like to order a coffee, please."
    assert body["scores"]["pronunciation"]["score"] == 80
    assert body["source"] == "RULE_ONLY"


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
