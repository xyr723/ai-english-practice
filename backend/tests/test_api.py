from fastapi.testclient import TestClient

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

