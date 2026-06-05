from typing import Dict, Optional


ScoreResult = Dict[str, Dict[str, object]]


def score_turn(
    word_count: int,
    duration_ms: int,
    asr_confidence: Optional[float],
    grammar_issue_count: int,
    spelling_issue_count: int,
    matched_goals: int,
    total_goals: int,
) -> ScoreResult:
    grammar = _clamp(100 - grammar_issue_count * 8 - spelling_issue_count * 4)
    fluency = _score_fluency(word_count, duration_ms)
    pronunciation = _score_pronunciation(asr_confidence)
    completion = _score_completion(matched_goals, total_goals)

    return {
        "grammar": {
            "score": grammar,
            "reason": f"Found {grammar_issue_count + spelling_issue_count} grammar or spelling issues.",
        },
        "fluency": {
            "score": fluency,
            "reason": _fluency_reason(word_count, duration_ms),
        },
        "pronunciation": {
            "score": pronunciation,
            "reason": "ASR confidence is unavailable."
            if asr_confidence is None
            else f"ASR confidence is {asr_confidence:.2f}.",
        },
        "completion": {
            "score": completion,
            "reason": f"Matched {matched_goals} of {total_goals} scene goals.",
        },
    }


def _score_fluency(word_count: int, duration_ms: int) -> int:
    if word_count <= 0 or duration_ms <= 0:
        return 0

    words_per_minute = word_count / (duration_ms / 60000)
    if 80 <= words_per_minute <= 140:
        return 100
    if words_per_minute < 80:
        return _clamp(round(100 - (80 - words_per_minute) * 2))
    return _clamp(round(100 - (words_per_minute - 140) * 1.5))


def _score_pronunciation(asr_confidence: Optional[float]) -> int:
    if asr_confidence is None:
        return 0
    return _clamp(round(asr_confidence * 100))


def _score_completion(matched_goals: int, total_goals: int) -> int:
    if total_goals <= 0:
        return 0
    return _clamp(round(matched_goals / total_goals * 100))


def _fluency_reason(word_count: int, duration_ms: int) -> str:
    if word_count <= 0 or duration_ms <= 0:
        return "No usable speaking duration was provided."
    words_per_minute = word_count / (duration_ms / 60000)
    return f"Speaking speed is {words_per_minute:.0f} words per minute."


def _clamp(value: int) -> int:
    return max(0, min(100, int(value)))

