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

