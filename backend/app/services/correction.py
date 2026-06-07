import re
import os
from typing import Callable, Dict, List, Optional, Sequence

import httpx


Issue = Dict[str, str]
LanguageToolMatch = Dict[str, object]
LanguageToolChecker = Callable[[str, str], List[LanguageToolMatch]]

DEFAULT_LANGUAGE_TOOL_URL = "https://api.languagetool.org/v2/check"
DEFAULT_LANGUAGE_TOOL_TIMEOUT_SECONDS = 4.0


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
    url = os.getenv("LANGUAGETOOL_URL", "").strip() or DEFAULT_LANGUAGE_TOOL_URL

    timeout_seconds = _language_tool_timeout_seconds()
    return LanguageToolClient(url=url, timeout_seconds=timeout_seconds)


def check_text(
    text: str,
    scenario_id: str = "",
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
        result["betterExpression"], result["issues"] = _apply_common_grammar_rules(
            str(result["betterExpression"]),
            result["issues"],
        )
        result["betterExpression"], result["issues"] = _apply_scenario_guidance(
            str(result["betterExpression"]),
            result["issues"],
            original,
            scenario_id,
            scenario_keywords,
        )
        return result

    try:
        matches = language_tool_checker(better, language)
    except LanguageToolUnavailable:
        result["betterExpression"], result["issues"] = _apply_common_grammar_rules(
            str(result["betterExpression"]),
            result["issues"],
        )
        result["betterExpression"], result["issues"] = _apply_scenario_guidance(
            str(result["betterExpression"]),
            result["issues"],
            original,
            scenario_id,
            scenario_keywords,
        )
        result["source"] = "RULE_FALLBACK"
        return result

    result["betterExpression"] = _apply_language_tool_replacements(better, matches)
    result["issues"] = issues + _language_tool_issues(matches)
    result["betterExpression"], result["issues"] = _apply_scenario_guidance(
        str(result["betterExpression"]),
        result["issues"],
        original,
        scenario_id,
        scenario_keywords,
    )
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


def _apply_common_grammar_rules(text: str, issues: List[Issue]) -> tuple[str, List[Issue]]:
    grammar_rules = (
        (
            re.compile(r"\bI\s+has\b", flags=re.IGNORECASE),
            "I have",
            "Use 'have' with the subject 'I'.",
        ),
        (
            re.compile(r"\bI\s+am\s+agree\b", flags=re.IGNORECASE),
            "I agree",
            "Use 'I agree' instead of 'I am agree'.",
        ),
        (
            re.compile(r"\bI\s+want\s+go\b", flags=re.IGNORECASE),
            "I want to go",
            "Use 'to' after 'want' before another verb.",
        ),
    )

    corrected = text
    updated_issues = list(issues)
    for pattern, replacement, message in grammar_rules:
        if pattern.search(corrected):
            corrected = pattern.sub(replacement, corrected)
            updated_issues.append(
                {
                    "type": "grammar",
                    "message": message,
                    "suggestion": replacement,
                }
            )
    return corrected, updated_issues


def _apply_scenario_guidance(
    text: str,
    issues: List[Issue],
    original: str,
    scenario_id: str,
    scenario_keywords: Sequence[str],
) -> tuple[str, List[Issue]]:
    if not scenario_id:
        return text, issues

    lowered = original.lower()
    scenario_key = _scenario_key(scenario_id, scenario_keywords)
    guidance = _scenario_guidance_for(scenario_key, lowered)
    if guidance is None:
        return text, issues

    message, suggestion, append_to_expression = guidance
    updated_text = text
    if append_to_expression and suggestion not in updated_text:
        updated_text = _append_sentence(updated_text, suggestion)

    return updated_text, issues + [
        {
            "type": "scenario",
            "message": message,
            "suggestion": suggestion,
        }
    ]


def _scenario_key(scenario_id: str, scenario_keywords: Sequence[str]) -> str:
    lowered_id = scenario_id.lower()
    keywords = {keyword.lower() for keyword in scenario_keywords}
    if "airport" in lowered_id or "flight" in keywords:
        return "airport"
    if "interview" in lowered_id or "project" in keywords:
        return "interview"
    if "meeting" in lowered_id or "timeline" in keywords or "risk" in keywords:
        return "meeting"
    if "restaurant" in lowered_id or "coffee" in keywords or "sandwich" in keywords:
        return "restaurant"
    if "shopping" in lowered_id or "price" in keywords or "discount" in keywords:
        return "shopping"
    if "library" in lowered_id or "book" in keywords or "borrow" in keywords:
        return "library"
    if "hospital" in lowered_id or "doctor" in keywords or "appointment" in keywords:
        return "hospital"
    return "custom"


def _scenario_guidance_for(scenario_key: str, lowered_text: str) -> Optional[tuple[str, str, bool]]:
    if scenario_key == "airport" and _missing_any(lowered_text, ("flight number", "flight no", "ticket number", "earlier", "later", "time")):
        return (
            "机场改签时建议补充航班号/机票信息，并说明想改到更早还是更晚的时间。可说：My flight number is ___, and I prefer an earlier/later flight.",
            "You can add: My flight number is ___, and I prefer an earlier/later flight.",
            True,
        )
    if scenario_key == "interview" and _missing_any(lowered_text, ("situation", "task", "action", "result", "impact", "outcome")):
        return (
            "面试回答建议用 STAR 结构补充情境、任务、行动和结果，不只说做过项目。",
            "You can use STAR: situation, task, action, and result.",
            True,
        )
    if scenario_key == "meeting" and _missing_any(lowered_text, ("risk", "next step", "owner", "deadline", "action item")):
        return (
            "会议表达建议同时说明 risk、负责人或 next step，方便团队推进。可说：The main risk is ___, and the next step is ___.",
            "You can add: The main risk is ___, and the next step is ___.",
            True,
        )
    if scenario_key == "restaurant" and _missing_any(lowered_text, ("for here", "takeaway", "to go", "size", "drink")):
        return (
            "点餐时除了礼貌表达，也可以补充堂食/外带、杯型或饮品搭配。",
            "Could I have it for here, please?",
            False,
        )
    if scenario_key == "shopping" and _missing_any(lowered_text, ("size", "color", "price", "discount", "try")):
        return (
            "购物场景建议说明尺码/颜色，并询问价格、折扣或是否可以试穿。",
            "You can add: Do you have this in my size, and is there any discount?",
            True,
        )
    if scenario_key == "library" and _missing_any(lowered_text, ("book", "author", "topic", "library card", "return date")):
        return (
            "借书场景建议说明书名、作者或主题，并确认借书证和归还日期。",
            "You can add: I am looking for a book about ___, and I have my library card.",
            True,
        )
    if scenario_key == "hospital" and _missing_any(lowered_text, ("appointment", "doctor", "department", "symptom", "register")):
        return (
            "医院场景建议说明症状、科室或是否预约，方便工作人员安排下一步。",
            "You can add: I have ___ symptoms, and I need to register for the right department.",
            True,
        )
    if scenario_key == "custom" and _missing_any(lowered_text, ("need", "want", "confirm", "question", "please")):
        return (
            "自定义场景建议先说明目标，再补充一个需要确认的细节。",
            "You can add: I need ___, and I would like to confirm ___.",
            True,
        )
    return None


def _missing_any(text: str, markers: Sequence[str]) -> bool:
    return not any(marker in text for marker in markers)


def _append_sentence(text: str, sentence: str) -> str:
    stripped = text.strip()
    if not stripped:
        return sentence
    if stripped[-1] not in ".!?":
        stripped += "."
    return f"{stripped} {sentence}"


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
