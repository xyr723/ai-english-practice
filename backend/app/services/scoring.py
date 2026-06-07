import re
from typing import Any, Dict, Optional, Sequence


ScoreResult = Dict[str, Dict[str, object]]


def score_turn(
    word_count: int,
    duration_ms: int,
    asr_confidence: Optional[float],
    grammar_issue_count: int,
    spelling_issue_count: int,
    matched_goals: int,
    total_goals: int,
    recognition_alternatives: Sequence[Dict[str, Any]] = (),
) -> ScoreResult:
    grammar = _clamp(100 - grammar_issue_count * 8 - spelling_issue_count * 4)
    fluency = _score_fluency(word_count, duration_ms)
    pronunciation, pronunciation_reason = _score_pronunciation(
        asr_confidence,
        word_count,
        duration_ms,
        recognition_alternatives,
    )
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
            "reason": pronunciation_reason,
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


def _score_pronunciation(
    asr_confidence: Optional[float],
    word_count: int,
    duration_ms: int,
    recognition_alternatives: Sequence[Dict[str, Any]],
) -> tuple[int, str]:
    if asr_confidence is None:
        return 0, "ASR confidence is unavailable, so pronunciation clarity cannot be assessed."

    base_score = _clamp(round(asr_confidence * 100))
    stability = _candidate_stability(recognition_alternatives)
    stability_penalty = int((100 - stability) * 0.32)
    tempo_penalty = _pronunciation_tempo_penalty(word_count, duration_ms)
    score = _clamp(base_score - stability_penalty - tempo_penalty)
    uncertain_words = _uncertain_words(recognition_alternatives)
    uncertain_part = (
        "candidate words were stable"
        if not uncertain_words
        else f"practice these unclear words: {', '.join(uncertain_words)}"
    )
    tempo_part = _pronunciation_tempo_reason(word_count, duration_ms)
    return (
        score,
        f"ASR confidence is {asr_confidence:.2f}; candidate stability is {stability}/100; "
        f"{uncertain_part}. {tempo_part}",
    )


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


def _candidate_stability(recognition_alternatives: Sequence[Dict[str, Any]]) -> int:
    transcripts = [_alternative_text(alternative) for alternative in recognition_alternatives]
    transcripts = [transcript for transcript in transcripts if transcript]
    if len(transcripts) <= 1:
        return 100

    primary_tokens = _content_tokens(transcripts[0])
    if not primary_tokens:
        return 100

    stability_scores = []
    for transcript in transcripts[1:]:
        tokens = set(_content_tokens(transcript))
        if not tokens:
            stability_scores.append(0)
            continue
        stability_scores.append(int(sum(1 for token in primary_tokens if token in tokens) * 100 / len(primary_tokens)))

    if not stability_scores:
        return 100
    return _clamp(round(sum(stability_scores) / len(stability_scores)))


def _uncertain_words(recognition_alternatives: Sequence[Dict[str, Any]]) -> list[str]:
    transcripts = [_alternative_text(alternative) for alternative in recognition_alternatives]
    transcripts = [transcript for transcript in transcripts if transcript]
    if len(transcripts) <= 1:
        return []

    primary_tokens = []
    for token in _content_tokens(transcripts[0]):
        if len(token) > 3 and token not in primary_tokens:
            primary_tokens.append(token)

    alternative_token_sets = [set(_content_tokens(transcript)) for transcript in transcripts[1:]]
    return [
        token
        for token in primary_tokens
        if any(token not in tokens for tokens in alternative_token_sets)
    ][:3]


def _alternative_text(alternative: Dict[str, Any]) -> str:
    value = alternative.get("transcript", "")
    return value if isinstance(value, str) else ""


def _content_tokens(text: str) -> list[str]:
    return [match.group(0).strip("'").lower() for match in re.finditer(r"[a-zA-Z']+", text) if match.group(0).strip("'")]


def _pronunciation_tempo_penalty(word_count: int, duration_ms: int) -> int:
    if word_count <= 0 or duration_ms <= 0:
        return 0
    words_per_minute = word_count / (duration_ms / 60000)
    if words_per_minute < 50 or words_per_minute > 170:
        return 6
    return 0


def _pronunciation_tempo_reason(word_count: int, duration_ms: int) -> str:
    if word_count <= 0 or duration_ms <= 0:
        return "No usable speaking tempo was provided."
    words_per_minute = word_count / (duration_ms / 60000)
    if words_per_minute < 50 or words_per_minute > 170:
        return f"Speaking tempo is {words_per_minute:.0f} words per minute; slow down and pronounce each word clearly."
    return "Speaking tempo is suitable for a clarity check."
