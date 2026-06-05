import re
from typing import Dict, List, Sequence


Issue = Dict[str, str]


def check_text(
    text: str,
    scenario_keywords: Sequence[str] = (),
    require_polite: bool = False,
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

    return {
        "original": original,
        "betterExpression": better,
        "issues": issues,
        "source": "RULE_ONLY",
    }


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

