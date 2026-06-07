import json
import os
from typing import Any, Dict, Mapping, Optional

import httpx


DEFAULT_DEEPSEEK_BASE_URL = "https://api.deepseek.com"
DEFAULT_DEEPSEEK_MODEL = "deepseek-v4-flash"
DEFAULT_DEEPSEEK_TIMEOUT_SECONDS = 8.0


class DeepSeekUnavailable(RuntimeError):
    pass


class DeepSeekCoachClient:
    def __init__(
        self,
        api_key: str,
        base_url: str = DEFAULT_DEEPSEEK_BASE_URL,
        model: str = DEFAULT_DEEPSEEK_MODEL,
        timeout_seconds: float = DEFAULT_DEEPSEEK_TIMEOUT_SECONDS,
    ):
        self.api_key = api_key
        self.base_url = base_url.rstrip("/")
        self.model = model
        self.timeout_seconds = timeout_seconds

    def analyze(
        self,
        scenario: Mapping[str, Any],
        request: Any,
        fallback: Mapping[str, Any],
    ) -> Dict[str, object]:
        payload = self._chat_completion(_messages(scenario, request, fallback), max_tokens=800)
        content = _message_content(payload)
        return _parse_coach_result(content, fallback)

    def summarize(
        self,
        scenario: Mapping[str, Any],
        request: Any,
        fallback: Mapping[str, Any],
    ) -> Dict[str, object]:
        payload = self._chat_completion(
            _summary_messages(scenario, request, fallback),
            max_tokens=900,
        )
        content = _message_content(payload)
        return _parse_summary_result(content, fallback)

    def _chat_completion(self, messages: list[dict[str, str]], max_tokens: int) -> Mapping[str, Any]:
        try:
            response = httpx.post(
                f"{self.base_url}/chat/completions",
                headers={
                    "Content-Type": "application/json",
                    "Authorization": f"Bearer {self.api_key}",
                },
                json={
                    "model": self.model,
                    "messages": messages,
                    "response_format": {"type": "json_object"},
                    "max_tokens": max_tokens,
                    "temperature": 0.3,
                    "stream": False,
                },
                timeout=self.timeout_seconds,
            )
            response.raise_for_status()
            return response.json()
        except (httpx.TimeoutException, httpx.HTTPError, ValueError) as exc:
            raise DeepSeekUnavailable("DeepSeek request failed") from exc


def create_deepseek_coach_client() -> Optional[DeepSeekCoachClient]:
    api_key = os.getenv("DEEPSEEK_API_KEY", "").strip()
    if not api_key:
        return None

    return DeepSeekCoachClient(
        api_key=api_key,
        base_url=os.getenv("DEEPSEEK_BASE_URL", "").strip() or DEFAULT_DEEPSEEK_BASE_URL,
        model=os.getenv("DEEPSEEK_MODEL", "").strip() or DEFAULT_DEEPSEEK_MODEL,
        timeout_seconds=_deepseek_timeout_seconds(),
    )


def _deepseek_timeout_seconds() -> float:
    raw_value = os.getenv("DEEPSEEK_TIMEOUT_SECONDS", "").strip()
    if not raw_value:
        return DEFAULT_DEEPSEEK_TIMEOUT_SECONDS
    try:
        return float(raw_value)
    except ValueError:
        return DEFAULT_DEEPSEEK_TIMEOUT_SECONDS


def _messages(
    scenario: Mapping[str, Any],
    request: Any,
    fallback: Mapping[str, Any],
) -> list[dict[str, str]]:
    system_prompt = """
You are an AI English speaking coach in an Android practice app.
Return JSON only. Do not add markdown.
The JSON format must be:
{
  "reply": "one short English role-play reply",
  "replyTranslation": "Chinese translation of reply",
  "betterExpression": "corrected and more natural English version of the user's sentence",
  "tips": ["1-3 concise coaching tips in Chinese"],
  "scores": {
    "grammar": {"score": 0-100, "reason": "short Chinese explanation"},
    "fluency": {"score": 0-100, "reason": "short Chinese explanation using durationMs when relevant"},
    "pronunciation": {"score": 0-100, "reason": "short Chinese explanation using asrConfidence when relevant"},
    "completion": {"score": 0-100, "reason": "short Chinese explanation"}
  }
}
Keep the conversation in the scenario role and ask one natural follow-up question when useful.
Only adjust scores when the transcript, durationMs, asrConfidence, and scenario evidence support it.
If uncertain, keep the ruleFallback scores.
""".strip()

    user_payload = {
        "scenario": {
            "id": str(scenario.get("id", "")),
            "name": str(scenario.get("name", "")),
            "role": str(scenario.get("role", "")),
            "opening": str(scenario.get("opening", "")),
            "goals": scenario.get("goals", []),
            "keywords": scenario.get("keywords", []),
        },
        "turnIndex": getattr(request, "turnIndex", 0),
        "turnText": getattr(request, "turnText", ""),
        "history": getattr(request, "history", [])[-6:],
        "durationMs": getattr(request, "durationMs", 0),
        "asrConfidence": getattr(request, "asrConfidence", None),
        "ruleFallback": {
            "reply": fallback.get("reply", ""),
            "replyTranslation": fallback.get("replyTranslation", ""),
            "betterExpression": fallback.get("betterExpression", ""),
            "tips": fallback.get("tips", []),
            "scores": fallback.get("scores", {}),
        },
    }
    return [
        {"role": "system", "content": system_prompt},
        {
            "role": "user",
            "content": "Please analyze this practice turn and return valid JSON:\n"
            + json.dumps(user_payload, ensure_ascii=False),
        },
    ]


def _summary_messages(
    scenario: Mapping[str, Any],
    request: Any,
    fallback: Mapping[str, Any],
) -> list[dict[str, str]]:
    system_prompt = """
You are an AI English speaking coach writing a practice summary.
Return JSON only. Do not add markdown.
The JSON format must be:
{
  "averageScore": 0-100,
  "strengths": ["1-2 concise strengths in Chinese"],
  "improvements": ["1-2 concrete improvement points in Chinese"],
  "nextGoal": "one actionable next goal in Chinese",
  "practicePlan": ["2-3 short practice actions in Chinese"]
}
Use the user's turns, corrections, score reasons, and scenario goals.
Keep the advice specific and suitable for a learner after one session.
""".strip()

    user_payload = {
        "scenario": {
            "id": str(scenario.get("id", "")),
            "name": str(scenario.get("name", "")),
            "role": str(scenario.get("role", "")),
            "goals": scenario.get("goals", []),
            "keywords": scenario.get("keywords", []),
        },
        "scenarioId": getattr(request, "scenarioId", ""),
        "turns": getattr(request, "turns", []),
        "ruleFallback": {
            "turnCount": fallback.get("turnCount", 0),
            "averageScore": fallback.get("averageScore", 0),
            "strengths": fallback.get("strengths", []),
            "improvements": fallback.get("improvements", []),
            "nextGoal": fallback.get("nextGoal", ""),
            "practicePlan": fallback.get("practicePlan", []),
        },
    }
    return [
        {"role": "system", "content": system_prompt},
        {
            "role": "user",
            "content": "Please write a practice summary and return valid JSON:\n"
            + json.dumps(user_payload, ensure_ascii=False),
        },
    ]


def _message_content(payload: Mapping[str, Any]) -> str:
    try:
        content = payload["choices"][0]["message"]["content"]
    except (KeyError, IndexError, TypeError) as exc:
        raise DeepSeekUnavailable("DeepSeek response is invalid") from exc

    if not isinstance(content, str) or not content.strip():
        raise DeepSeekUnavailable("DeepSeek response content is empty")
    return content


def _parse_coach_result(content: str, fallback: Mapping[str, Any]) -> Dict[str, object]:
    try:
        payload = json.loads(content)
    except json.JSONDecodeError:
        payload = json.loads(_extract_json_object(content))

    if not isinstance(payload, dict):
        raise DeepSeekUnavailable("DeepSeek response JSON is invalid")

    return {
        "reply": _string_or_fallback(payload.get("reply"), fallback.get("reply", "")),
        "replyTranslation": _string_or_fallback(
            payload.get("replyTranslation"),
            fallback.get("replyTranslation", ""),
        ),
        "betterExpression": _string_or_fallback(
            payload.get("betterExpression"),
            fallback.get("betterExpression", ""),
        ),
        "tips": _tips_or_fallback(payload.get("tips"), fallback.get("tips", [])),
        "scores": _scores_or_fallback(payload.get("scores"), fallback.get("scores", {})),
    }


def _parse_summary_result(content: str, fallback: Mapping[str, Any]) -> Dict[str, object]:
    try:
        payload = json.loads(content)
    except json.JSONDecodeError:
        payload = json.loads(_extract_json_object(content))

    if not isinstance(payload, dict):
        raise DeepSeekUnavailable("DeepSeek summary JSON is invalid")

    return {
        "turnCount": _int_or_fallback(fallback.get("turnCount"), 0),
        "averageScore": _int_or_fallback(payload.get("averageScore"), fallback.get("averageScore", 0)),
        "strengths": _string_list_or_fallback(payload.get("strengths"), fallback.get("strengths", [])),
        "improvements": _string_list_or_fallback(
            payload.get("improvements"),
            fallback.get("improvements", []),
        ),
        "nextGoal": _string_or_fallback(payload.get("nextGoal"), fallback.get("nextGoal", "")),
        "practicePlan": _string_list_or_fallback(
            payload.get("practicePlan"),
            fallback.get("practicePlan", []),
        ),
        "scoreBreakdown": _list_or_fallback(fallback.get("scoreBreakdown"), []),
        "turnReviews": _list_or_fallback(fallback.get("turnReviews"), []),
    }


def _extract_json_object(content: str) -> str:
    start = content.find("{")
    end = content.rfind("}")
    if start < 0 or end <= start:
        raise DeepSeekUnavailable("DeepSeek response JSON is missing")
    return content[start : end + 1]


def _string_or_fallback(value: object, fallback: object) -> str:
    if isinstance(value, str) and value.strip():
        return value.strip()
    if isinstance(fallback, str):
        return fallback
    return ""


def _tips_or_fallback(value: object, fallback: object) -> list[str]:
    if isinstance(value, list):
        tips = [str(item).strip() for item in value if str(item).strip()]
        if tips:
            return tips[:3]
    if isinstance(fallback, list):
        return [str(item) for item in fallback]
    return []


def _string_list_or_fallback(value: object, fallback: object) -> list[str]:
    if isinstance(value, list):
        items = [str(item).strip() for item in value if str(item).strip()]
        if items:
            return items[:5]
    if isinstance(fallback, list):
        return [str(item) for item in fallback if str(item).strip()]
    return []


def _list_or_fallback(value: object, fallback: object) -> list[object]:
    if isinstance(value, list):
        return value
    if isinstance(fallback, list):
        return fallback
    return []


def _scores_or_fallback(value: object, fallback: object) -> dict[str, dict[str, object]]:
    candidate_scores = value if isinstance(value, Mapping) else {}
    fallback_scores = fallback if isinstance(fallback, Mapping) else {}
    return {
        key: _score_detail_or_fallback(candidate_scores.get(key), fallback_scores.get(key))
        for key in ("grammar", "fluency", "pronunciation", "completion")
    }


def _score_detail_or_fallback(value: object, fallback: object) -> dict[str, object]:
    if isinstance(value, Mapping):
        score = _score_or_none(value.get("score"))
        reason = value.get("reason")
        if score is not None and isinstance(reason, str) and reason.strip():
            return {"score": score, "reason": reason.strip()}

    if isinstance(fallback, Mapping):
        fallback_score = _int_or_fallback(fallback.get("score"), 0)
        fallback_reason = _string_or_fallback(fallback.get("reason"), "")
        return {"score": fallback_score, "reason": fallback_reason}

    return {"score": 0, "reason": ""}


def _score_or_none(value: object) -> int | None:
    if isinstance(value, bool):
        return None
    try:
        score = int(value)
    except (TypeError, ValueError):
        return None
    if score < 0 or score > 100:
        return None
    return score


def _int_or_fallback(value: object, fallback: object) -> int:
    try:
        return max(0, min(100, int(value)))
    except (TypeError, ValueError):
        try:
            return max(0, min(100, int(fallback)))
        except (TypeError, ValueError):
            return 0
