from app.services.scoring import score_turn


def test_score_turn_returns_explainable_dimensions():
    scores = score_turn(
        word_count=10,
        duration_ms=6000,
        asr_confidence=0.8,
        grammar_issue_count=2,
        spelling_issue_count=0,
        matched_goals=2,
        total_goals=3,
    )

    assert scores["grammar"]["score"] == 84
    assert scores["fluency"]["score"] == 100
    assert scores["pronunciation"]["score"] == 80
    assert scores["completion"]["score"] == 67
    assert all(value["reason"] for value in scores.values())


def test_score_turn_handles_empty_duration_and_goal_counts():
    scores = score_turn(
        word_count=0,
        duration_ms=0,
        asr_confidence=None,
        grammar_issue_count=0,
        spelling_issue_count=0,
        matched_goals=0,
        total_goals=0,
    )

    assert scores["fluency"]["score"] == 0
    assert scores["pronunciation"]["score"] == 0
    assert scores["completion"]["score"] == 0


def test_score_turn_uses_recognition_stability_for_pronunciation():
    scores = score_turn(
        word_count=6,
        duration_ms=6000,
        asr_confidence=0.82,
        grammar_issue_count=0,
        spelling_issue_count=0,
        matched_goals=2,
        total_goals=3,
        recognition_alternatives=[
            {"transcript": "I want to order a coffee", "confidence": 0.82},
            {"transcript": "I want to order a copy", "confidence": 0.58},
            {"transcript": "I want order coffee", "confidence": 0.45},
        ],
    )

    assert scores["pronunciation"]["score"] == 74
    assert "candidate stability" in scores["pronunciation"]["reason"]
    assert "coffee" in scores["pronunciation"]["reason"]
