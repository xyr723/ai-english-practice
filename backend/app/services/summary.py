from typing import Any, Dict, List, Mapping


SCORE_DIMENSIONS = (
    ("grammar", "语法"),
    ("fluency", "流利度"),
    ("pronunciation", "发音"),
    ("completion", "完成度"),
)


def summarize_practice(
    turns: List[Dict[str, Any]],
    scenario: Mapping[str, Any] | None = None,
    source: str = "RULE_ONLY",
) -> Dict[str, object]:
    if not turns:
        return {
            "source": source,
            "turnCount": 0,
            "averageScore": 0,
            "strengths": [],
            "improvements": ["Complete at least one speaking turn."],
            "nextGoal": "Start with a short ordering sentence.",
            "scoreBreakdown": _empty_score_breakdown(),
            "turnReviews": [],
            "practicePlan": ["Complete one short answer in the selected scene."],
        }

    scores = [_turn_average(turn) for turn in turns]
    average = round(sum(scores) / len(scores)) if scores else 0
    score_breakdown = _score_breakdown(turns)

    return {
        "source": source,
        "turnCount": len(turns),
        "averageScore": average,
        "strengths": _strengths(turns, score_breakdown),
        "improvements": _improvements(turns, score_breakdown),
        "nextGoal": _next_goal(scenario),
        "scoreBreakdown": score_breakdown,
        "turnReviews": _turn_reviews(turns),
        "practicePlan": _practice_plan(),
    }


def _turn_average(turn: Dict[str, Any]) -> int:
    score_values = turn.get("scores", {})
    if not isinstance(score_values, dict) or not score_values:
        return 0
    numeric_scores = [
        score
        for key, _ in SCORE_DIMENSIONS
        for score in [_score_value(score_values, key)]
        if score is not None
    ]
    if not numeric_scores:
        return 0
    return round(sum(numeric_scores) / len(numeric_scores))


def _score_breakdown(turns: List[Dict[str, Any]]) -> List[Dict[str, object]]:
    rows = []
    for key, label in SCORE_DIMENSIONS:
        values = []
        reasons = []
        for turn in turns:
            score_values = turn.get("scores", {})
            if not isinstance(score_values, dict):
                continue
            score = _score_value(score_values, key)
            if score is not None:
                values.append(score)
            reason = _score_reason(score_values, key)
            if reason:
                reasons.append(reason)

        rows.append(
            {
                "label": label,
                "score": round(sum(values) / len(values)) if values else 0,
                "reason": reasons[0] if reasons else "No score reason was provided.",
            }
        )
    return rows


def _empty_score_breakdown() -> List[Dict[str, object]]:
    return [
        {"label": label, "score": 0, "reason": "No completed speaking turn yet."}
        for _, label in SCORE_DIMENSIONS
    ]


def _turn_reviews(turns: List[Dict[str, Any]]) -> List[Dict[str, object]]:
    return [
        {
            "index": index + 1,
            "userText": str(turn.get("userText", "")),
            "betterExpression": str(turn.get("betterExpression", "")),
            "reply": str(turn.get("reply", "")),
            "score": _turn_average(turn),
            "tips": _string_list(turn.get("tips", [])),
        }
        for index, turn in enumerate(turns)
    ]


def _strengths(turns: List[Dict[str, Any]], score_breakdown: List[Dict[str, object]]) -> List[str]:
    best = max(score_breakdown, key=lambda item: int(item.get("score", 0)))
    return [
        f"You completed {len(turns)} speaking turn(s).",
        f"Your strongest area was {best['label']} at {best['score']} points.",
    ]


def _improvements(turns: List[Dict[str, Any]], score_breakdown: List[Dict[str, object]]) -> List[str]:
    weakest = min(score_breakdown, key=lambda item: int(item.get("score", 0)))
    first_tip = next(
        (tip for turn in turns for tip in _string_list(turn.get("tips", []))),
        "Review corrected expressions and repeat the scene.",
    )
    return [
        f"Focus next on {weakest['label']} at {weakest['score']} points.",
        first_tip,
    ]


def _next_goal(scenario: Mapping[str, Any] | None) -> str:
    scenario_id = str((scenario or {}).get("id", ""))
    if scenario_id == "restaurant":
        return "Practice confirming drink size, price, and takeaway options."
    if scenario_id == "interview":
        return "Practice one STAR answer with situation, action, and result."
    if scenario_id == "meeting":
        return "Practice giving an opinion and confirming the next action item."
    return "Repeat the same scene and complete one more detailed answer."


def _practice_plan() -> List[str]:
    return [
        "Read the better expression aloud three times.",
        "Repeat the same scene for at least two more turns.",
        "Use one polite expression and one follow-up detail next time.",
    ]


def _score_value(scores: Dict[str, Any], key: str) -> int | None:
    value = scores.get(key)
    if isinstance(value, dict):
        value = value.get("score")
    if isinstance(value, int):
        return max(0, min(100, value))
    if isinstance(value, float):
        return max(0, min(100, round(value)))
    return None


def _score_reason(scores: Dict[str, Any], key: str) -> str:
    value = scores.get(key)
    if isinstance(value, dict):
        reason = value.get("reason")
        if isinstance(reason, str):
            return reason
    return ""


def _string_list(value: Any) -> List[str]:
    if not isinstance(value, list):
        return []
    return [str(item) for item in value if str(item).strip()]
