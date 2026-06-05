import re
import os
from typing import Callable, Dict, List, Optional, Sequence

import httpx


Issue = Dict[str, str]
LanguageToolMatch = Dict[str, object]
LanguageToolChecker = Callable[[str, str], List[LanguageToolMatch]]

DEFAULT_LANGUAGE_TOOL_TIMEOUT_SECONDS = 1.2


class LanguageToolUnavailable(RuntimeError):
    pass


class LanguageToolClient:
    def __init__(self, url: str, timeout_seconds: float = DEFAULT_LANGUAGE_TOOL_TIMEOUT_SECONDS):
        self.url = url
        self.timeout_seconds = timeout_seconds

    def __call__(self, text: str, language: str) -> List[LanguageToolMatch]:
        try:
            response = httpx.post(
                self.url,
                data={"text": text, "language": language},
                timeout=self.timeout_seconds,
            )
            response.raise_for_status()
            payload = response.json()
        except httpx.TimeoutException as exc:
            raise LanguageToolUnavailable("LanguageTool timed out") from exc
        except (httpx.HTTPError, ValueError) as exc:
            raise LanguageToolUnavailable("LanguageTool request failed") from exc

        matches = payload.get("matches", [])
        if not isinstance(matches, list):
            raise LanguageToolUnavailable("LanguageTool response is invalid")
        return matches


def create_language_tool_checker() -> Optional[LanguageToolChecker]:
    url = os.getenv("LANGUAGETOOL_URL", "").strip()
    if not url:
        return None

    timeout_seconds = _language_tool_timeout_seconds()
    return LanguageToolClient(url=url, timeout_seconds=timeout_seconds)


def check_text(
    text: str,
    scenario_keywords: Sequence[str] = (),
    require_polite: bool = False,
    language: str = "en-US",
    language_tool_checker: Optional[LanguageToolChecker] = None,
) -> Dict[str, object]:
    original = text.strip()
    better = original
    issues: List[Issue] = []

    if re.search(r"\bI\s+want\s+order\b", better, flags=re.IGNORECASE):
        better = re.sub(
            r"\bI\s+want\s+order\b",
            "I'd like to order",
            better,
            flags=re.IGNORECASE,
        )
        issues.append(
            {
                "type": "grammar",
                "message": "Use 'to order' or a polite ordering expression.",
                "suggestion": "I'd like to order",
            }
        )

    if _mentions_order_item(better, scenario_keywords):
        better = _add_missing_article(better)

    if require_polite and not _has_polite_marker(original):
        better = _append_please(better)
        issues.append(
            {
                "type": "politeness",
                "message": "Add a polite expression in ordering scenes.",
                "suggestion": "please",
            }
        )
    else:
        better = _ensure_sentence_punctuation(better)

    result = {
        "original": original,
        "betterExpression": better,
        "issues": issues,
        "source": "RULE_ONLY",
    }

    if language_tool_checker is None:
        return result

    try:
        matches = language_tool_checker(better, language)
    except LanguageToolUnavailable:
        result["source"] = "RULE_FALLBACK"
        return result

    result["betterExpression"] = _apply_language_tool_replacements(better, matches)
    result["issues"] = issues + _language_tool_issues(matches)
    result["source"] = "LANGUAGE_TOOL"
    return result


def _language_tool_timeout_seconds() -> float:
    raw_value = os.getenv("LANGUAGETOOL_TIMEOUT_SECONDS", "").strip()
    if not raw_value:
        return DEFAULT_LANGUAGE_TOOL_TIMEOUT_SECONDS
    try:
        return float(raw_value)
    except ValueError:
        return DEFAULT_LANGUAGE_TOOL_TIMEOUT_SECONDS


def _apply_language_tool_replacements(text: str, matches: Sequence[LanguageToolMatch]) -> str:
    corrected = text
    for match in sorted(matches, key=_match_offset, reverse=True):
        replacement = _first_replacement(match)
        if not replacement:
            continue
        offset = _match_offset(match)
        length = _match_length(match)
        if offset < 0 or length <= 0 or offset + length > len(corrected):
            continue
        corrected = corrected[:offset] + replacement + corrected[offset + length:]
    return corrected


def _language_tool_issues(matches: Sequence[LanguageToolMatch]) -> List[Issue]:
    issues: List[Issue] = []
    for match in matches:
        issues.append(
            {
                "type": _language_tool_issue_type(match),
                "message": str(match.get("message", "LanguageTool suggested a correction.")),
                "suggestion": _first_replacement(match),
            }
        )
    return issues


def _language_tool_issue_type(match: LanguageToolMatch) -> str:
    rule = match.get("rule", {})
    category = rule.get("category", {}) if isinstance(rule, dict) else {}
    category_id = str(category.get("id", "")).lower() if isinstance(category, dict) else ""
    if "typo" in category_id or "spell" in category_id:
        return "spelling"
    if "style" in category_id:
        return "style"
    return "grammar"


def _first_replacement(match: LanguageToolMatch) -> str:
    replacements = match.get("replacements", [])
    if not isinstance(replacements, list) or not replacements:
        return ""
    first = replacements[0]
    if not isinstance(first, dict):
        return ""
    return str(first.get("value", ""))


def _match_offset(match: LanguageToolMatch) -> int:
    return _safe_int(match.get("offset", -1), -1)


def _match_length(match: LanguageToolMatch) -> int:
    return _safe_int(match.get("length", 0), 0)


def _safe_int(value: object, default: int) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return default


def _mentions_order_item(text: str, scenario_keywords: Sequence[str]) -> bool:
    lowered = text.lower()
    return any(keyword.lower() in lowered for keyword in scenario_keywords)


def _add_missing_article(text: str) -> str:
    return re.sub(
        r"\border\s+(coffee|tea|sandwich)\b",
        r"order a \1",
        text,
        flags=re.IGNORECASE,
    )


def _has_polite_marker(text: str) -> bool:
    lowered = text.lower()
    return any(marker in lowered for marker in ("please", "could i", "would like", "i'd like"))


def _append_please(text: str) -> str:
    stripped = text.strip().rstrip(".!?")
    if stripped.endswith(", please"):
        return stripped + "."
    return stripped + ", please."


def _ensure_sentence_punctuation(text: str) -> str:
    stripped = text.strip()
    if not stripped:
        return stripped
    if stripped[-1] in ".!?":
        return stripped
    return stripped + "."
