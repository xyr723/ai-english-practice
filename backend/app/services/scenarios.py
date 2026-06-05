import json
from pathlib import Path
from typing import Dict, List


REPO_ROOT = Path(__file__).resolve().parents[3]
SCENARIO_DIR = REPO_ROOT / "docs" / "scenarios"
REQUIRED_FIELDS = {"id", "name", "role", "opening", "goals", "turns", "fallbackReplies"}


def load_scenario(scenario_id: str) -> Dict[str, object]:
    path = SCENARIO_DIR / f"{scenario_id}.json"
    with path.open(encoding="utf-8") as file:
        scenario = json.load(file)

    missing = REQUIRED_FIELDS - set(scenario)
    if missing:
        missing_fields = ", ".join(sorted(missing))
        raise ValueError(f"Scenario {scenario_id} missing fields: {missing_fields}")

    if not scenario["turns"]:
        raise ValueError(f"Scenario {scenario_id} must contain at least one turn")
    if not scenario["fallbackReplies"]:
        raise ValueError(f"Scenario {scenario_id} must contain fallback replies")

    return scenario


def list_scenarios() -> List[Dict[str, str]]:
    items = []
    for path in sorted(SCENARIO_DIR.glob("*.json")):
        scenario = load_scenario(path.stem)
        items.append(
            {
                "id": str(scenario["id"]),
                "name": str(scenario["name"]),
                "role": str(scenario["role"]),
                "level": str(scenario.get("level", "")),
            }
        )
    return items


def next_reply(scenario: Dict[str, object], turn_text: str, turn_index: int) -> str:
    turns = scenario.get("turns", [])
    if isinstance(turns, list) and 0 <= turn_index < len(turns):
        return str(turns[turn_index]["reply"])

    fallback_replies = scenario.get("fallbackReplies", [])
    if isinstance(fallback_replies, list) and fallback_replies:
        return str(fallback_replies[0])

    return "Could you say that again, please?"


def next_reply_translation(scenario: Dict[str, object], turn_index: int) -> str:
    turns = scenario.get("turns", [])
    if isinstance(turns, list) and 0 <= turn_index < len(turns):
        return str(turns[turn_index].get("replyTranslation", ""))

    fallback_translations = scenario.get("fallbackReplyTranslations", [])
    if isinstance(fallback_translations, list) and fallback_translations:
        return str(fallback_translations[0])

    return ""


def matched_goals(scenario: Dict[str, object], text: str) -> List[str]:
    lowered = text.lower()
    goals = scenario.get("goals", [])
    keywords = [str(keyword).lower() for keyword in scenario.get("keywords", [])]
    matched: List[str] = []

    if "order_food_or_drink" in goals and any(keyword in lowered for keyword in keywords):
        matched.append("order_food_or_drink")

    if "use_polite_expression" in goals and _has_polite_expression(lowered):
        matched.append("use_polite_expression")

    if "answer_follow_up_question" in goals and _answers_follow_up(lowered):
        matched.append("answer_follow_up_question")

    return matched


def _has_polite_expression(lowered_text: str) -> bool:
    return any(marker in lowered_text for marker in ("please", "could i", "would like", "i'd like"))


def _answers_follow_up(lowered_text: str) -> bool:
    markers = ("yes", "no", "for here", "takeaway", "to go")
    return any(marker in lowered_text for marker in markers)
