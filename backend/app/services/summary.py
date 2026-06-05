from typing import Any, Dict, List


def summarize_practice(turns: List[Dict[str, Any]]) -> Dict[str, object]:
    if not turns:
        return {
            "averageScore": 0,
            "strengths": [],
            "improvements": ["Complete at least one speaking turn."],
            "nextGoal": "Start with a short ordering sentence.",
        }

    scores = [_turn_average(turn) for turn in turns]
    average = round(sum(scores) / len(scores)) if scores else 0

    return {
        "averageScore": average,
        "strengths": ["You completed the practice flow."],
        "improvements": ["Review corrected expressions and repeat the scene."],
        "nextGoal": "Practice confirming price and takeaway options.",
    }


def _turn_average(turn: Dict[str, Any]) -> int:
    score_values = turn.get("scores", {})
    if not isinstance(score_values, dict) or not score_values:
        return 0
    numeric_scores = [int(value) for value in score_values.values() if isinstance(value, int)]
    if not numeric_scores:
        return 0
    return round(sum(numeric_scores) / len(numeric_scores))

