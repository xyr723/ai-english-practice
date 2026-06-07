import json
from pathlib import Path
from typing import Dict, List


REPO_ROOT = Path(__file__).resolve().parents[3]
SCENARIO_DIR = REPO_ROOT / "docs" / "scenarios"
REQUIRED_FIELDS = {"id", "name", "role", "opening", "goals", "turns", "fallbackReplies"}
ANDROID_BUILTIN_SCENARIO_IDS = ("interview", "meeting")


def load_scenario(scenario_id: str) -> Dict[str, object]:
    path = SCENARIO_DIR / f"{scenario_id}.json"
    if path.exists():
        with path.open(encoding="utf-8") as file:
            scenario = json.load(file)
    else:
        scenario = _generated_scenario(scenario_id)

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
    for scenario_id in ANDROID_BUILTIN_SCENARIO_IDS:
        scenario = load_scenario(scenario_id)
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
    override_index = _reply_override_index(scenario, turn_text, turn_index)
    if isinstance(turns, list) and override_index is not None and 0 <= override_index < len(turns):
        return str(turns[override_index]["reply"])

    if isinstance(turns, list) and 0 <= turn_index < len(turns):
        return str(turns[turn_index]["reply"])

    fallback_replies = scenario.get("fallbackReplies", [])
    if isinstance(fallback_replies, list) and fallback_replies:
        return str(fallback_replies[0])

    return "Could you say that again, please?"


def next_reply_translation(scenario: Dict[str, object], turn_index: int, turn_text: str = "") -> str:
    turns = scenario.get("turns", [])
    override_index = _reply_override_index(scenario, turn_text, turn_index)
    if isinstance(turns, list) and override_index is not None and 0 <= override_index < len(turns):
        return str(turns[override_index].get("replyTranslation", ""))

    if isinstance(turns, list) and 0 <= turn_index < len(turns):
        return str(turns[turn_index].get("replyTranslation", ""))

    fallback_translations = scenario.get("fallbackReplyTranslations", [])
    if isinstance(fallback_translations, list) and fallback_translations:
        return str(fallback_translations[0])

    return ""


def _reply_override_index(scenario: Dict[str, object], turn_text: str, turn_index: int):
    if scenario.get("id") != "restaurant" or turn_index != 0:
        return None

    lowered = turn_text.lower()
    has_drink = any(marker in lowered for marker in ("tea", "coffee", "water", "juice", "drink", "cola"))
    has_food = any(marker in lowered for marker in ("hamburger", "burger", "sandwich", "cake", "salad", "food"))
    if has_drink and has_food:
        return 1

    return None


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

    for goal in goals:
        if goal in matched:
            continue
        if _matches_goal(goal, lowered, keywords):
            matched.append(str(goal))

    if not matched and keywords and any(keyword in lowered for keyword in keywords):
        matched.append(str(goals[0]))

    return matched


def _has_polite_expression(lowered_text: str) -> bool:
    return any(marker in lowered_text for marker in ("please", "could i", "would like", "i'd like"))


def _answers_follow_up(lowered_text: str) -> bool:
    markers = ("yes", "no", "for here", "takeaway", "to go")
    return any(marker in lowered_text for marker in markers)


def _matches_goal(goal: str, lowered_text: str, keywords: List[str]) -> bool:
    markers = {
        "introduce_self": ("my name", "i am", "i'm", "student", "developer"),
        "describe_experience": ("experience", "project", "worked", "built", "team"),
        "give_opinion": ("i think", "i believe", "should", "agree"),
        "ask_clarifying_question": ("?", "what", "how", "could you", "risk"),
        "confirm_next_step": ("next step", "confirm", "assign", "will", "follow up", "plan", "return"),
        "explain_request": ("need", "want", "looking for", "help", "change", "buy", "borrow"),
        "provide_flight_info": ("flight", "ticket", "time", "earlier", "later", "rebook"),
        "provide_book_info": ("book", "topic", "author", "library", "card"),
    }.get(goal, ())

    return any(marker in lowered_text for marker in markers) or (
        goal == "explain_request" and any(keyword in lowered_text for keyword in keywords)
    )


def _generated_scenario(scenario_id: str) -> Dict[str, object]:
    if scenario_id == "interview":
        return _interview_scenario()
    if scenario_id == "meeting":
        return _meeting_scenario()
    if scenario_id.startswith("custom-"):
        return _custom_scenario(scenario_id)
    raise FileNotFoundError(f"Scenario not found: {scenario_id}")


def _interview_scenario() -> Dict[str, object]:
    return {
        "id": "interview",
        "name": "Interview Practice",
        "role": "Interviewer",
        "level": "A2-B1",
        "opening": "Thanks for joining today. Could you briefly introduce yourself?",
        "openingTranslation": "感谢你今天来参加面试。可以简要介绍一下自己吗？",
        "goals": ["introduce_self", "describe_experience", "answer_follow_up_question"],
        "keywords": ["experience", "project", "team", "strength", "learn"],
        "turns": [
            _turn(
                "interview-turn-1",
                "introduce_self",
                "Good. Could you tell me about one project you worked on?",
                "很好。你能讲一个你参与过的项目吗？",
            ),
            _turn(
                "interview-turn-2",
                "describe_experience",
                "What was your main contribution to that project?",
                "你在那个项目中的主要贡献是什么？",
            ),
            _turn(
                "interview-turn-3",
                "answer_follow_up_question",
                "Thanks. What would you like to improve next?",
                "谢谢。接下来你想提升哪方面？",
            ),
        ],
        "fallbackReplies": [
            "Please answer with one concrete example.",
            "Could you make your answer a little more specific?",
        ],
        "fallbackReplyTranslations": [
            "请用一个具体例子来回答。",
            "你能把回答说得更具体一点吗？",
        ],
    }


def _meeting_scenario() -> Dict[str, object]:
    return {
        "id": "meeting",
        "name": "Meeting Discussion",
        "role": "Project lead",
        "level": "B1",
        "opening": "Let's review the plan. What should we discuss first?",
        "openingTranslation": "我们来回顾一下计划。应该先讨论什么？",
        "goals": ["give_opinion", "ask_clarifying_question", "confirm_next_step"],
        "keywords": ["timeline", "risk", "plan", "next step", "agree"],
        "turns": [
            _turn("meeting-turn-1", "give_opinion", "Thanks. What is the main timeline risk?", "谢谢。主要的时间风险是什么？"),
            _turn(
                "meeting-turn-2",
                "ask_clarifying_question",
                "That makes sense. What next step should we assign?",
                "有道理。我们应该安排什么下一步？",
            ),
            _turn(
                "meeting-turn-3",
                "confirm_next_step",
                "Great. I will note that as the action item.",
                "很好。我会把它记录为行动项。",
            ),
        ],
        "fallbackReplies": [
            "Could you share one concrete point for the meeting?",
            "Please explain the risk or next step in one sentence.",
        ],
        "fallbackReplyTranslations": [
            "你能分享一个会议中的具体观点吗？",
            "请用一句话说明风险或下一步。",
        ],
    }


def _custom_scenario(scenario_id: str) -> Dict[str, object]:
    _, theme, prompt = _custom_parts(scenario_id)
    templates = {
        "airport": _custom_airport_scenario,
        "shopping": _custom_shopping_scenario,
        "hospital": _custom_hospital_scenario,
        "library": _custom_library_scenario,
    }
    return templates.get(theme, _custom_generic_scenario)(scenario_id, prompt)


def _custom_parts(scenario_id: str) -> tuple[str, str, str]:
    parts = scenario_id.split("-", 2)
    theme = parts[1] if len(parts) > 1 and parts[1] else "custom"
    prompt = parts[2] if len(parts) > 2 and parts[2] else "custom scene"
    return parts[0], theme, prompt


def _custom_airport_scenario(scenario_id: str, prompt: str) -> Dict[str, object]:
    return {
        "id": scenario_id,
        "name": prompt,
        "role": "Airline staff",
        "level": "A2-B1",
        "opening": "Hello. How can I help you with your flight today?",
        "openingTranslation": "您好。今天我能为您的航班提供什么帮助？",
        "goals": ["explain_request", "provide_flight_info", "confirm_next_step"],
        "keywords": ["flight", "change", "ticket", "time", "please", "rebook"],
        "turns": [
            _turn("custom-airport-turn-1", "explain_request", "Sure. Which flight would you like to change?", "当然。您想改签哪一班航班？"),
            _turn(
                "custom-airport-turn-2",
                "provide_flight_info",
                "I can check that. Do you prefer an earlier or later flight?",
                "我可以帮您查询。您更想改到更早还是更晚的航班？",
            ),
            _turn(
                "custom-airport-turn-3",
                "confirm_next_step",
                "Great. Please confirm the new time before I update the ticket.",
                "好的。更新机票前，请确认新的时间。",
            ),
        ],
        "fallbackReplies": ["Could you tell me which flight you need to change?"],
        "fallbackReplyTranslations": ["请告诉我您需要改签哪一班航班。"],
    }


def _custom_shopping_scenario(scenario_id: str, prompt: str) -> Dict[str, object]:
    return {
        "id": scenario_id,
        "name": prompt,
        "role": "Shop assistant",
        "level": "A2-B1",
        "opening": "Hello. What are you looking for today?",
        "openingTranslation": "您好。今天您想买什么？",
        "goals": ["explain_request", "use_polite_expression", "confirm_next_step"],
        "keywords": ["price", "size", "discount", "bag", "please", "buy"],
        "turns": [
            _turn("custom-shopping-turn-1", "explain_request", "Sure. What size or color do you prefer?", "当然。您更喜欢什么尺码或颜色？"),
            _turn("custom-shopping-turn-2", "confirm_next_step", "I can check the price. Would you like to try it first?", "我可以查一下价格。您想先试一下吗？"),
        ],
        "fallbackReplies": ["Could you tell me what item you want to buy?"],
        "fallbackReplyTranslations": ["请告诉我您想买什么商品。"],
    }


def _custom_hospital_scenario(scenario_id: str, prompt: str) -> Dict[str, object]:
    return {
        "id": scenario_id,
        "name": prompt,
        "role": "Clinic receptionist",
        "level": "A2-B1",
        "opening": "Hello. How can I help you at the clinic today?",
        "openingTranslation": "您好。今天在诊所我能帮您什么？",
        "goals": ["explain_request", "use_polite_expression", "confirm_next_step"],
        "keywords": ["appointment", "doctor", "clinic", "register", "please", "sick"],
        "turns": [
            _turn("custom-hospital-turn-1", "explain_request", "Sure. Do you need an appointment or registration?", "当然。您需要预约还是挂号？"),
            _turn("custom-hospital-turn-2", "confirm_next_step", "Please confirm your name and the department you need.", "请确认您的姓名和需要就诊的科室。"),
        ],
        "fallbackReplies": ["Could you explain what help you need at the clinic?"],
        "fallbackReplyTranslations": ["请说明您在诊所需要什么帮助。"],
    }


def _custom_library_scenario(scenario_id: str, prompt: str) -> Dict[str, object]:
    return {
        "id": scenario_id,
        "name": prompt,
        "role": "Librarian",
        "level": "A2-B1",
        "opening": "Hello. What kind of book would you like to borrow today?",
        "openingTranslation": "您好。今天您想借哪类书？",
        "goals": ["explain_request", "provide_book_info", "confirm_next_step"],
        "keywords": ["book", "borrow", "return", "library", "card", "please"],
        "turns": [
            _turn("custom-library-turn-1", "explain_request", "Sure. Which book or topic are you looking for?", "当然。您想找哪本书或哪个主题？"),
            _turn("custom-library-turn-2", "provide_book_info", "I can check the shelf. Do you have a library card?", "我可以查一下书架。您有借书证吗？"),
            _turn("custom-library-turn-3", "confirm_next_step", "Great. Please confirm the return date before you borrow it.", "好的。借走前请确认归还日期。"),
        ],
        "fallbackReplies": ["Could you tell me which book or topic you need?"],
        "fallbackReplyTranslations": ["请告诉我您需要哪本书或哪个主题。"],
    }


def _custom_generic_scenario(scenario_id: str, prompt: str) -> Dict[str, object]:
    return {
        "id": scenario_id,
        "name": prompt,
        "role": "Scenario coach",
        "level": "A2-B1",
        "opening": f"Hello. Let's practice this situation: {prompt}. What would you like to say first?",
        "openingTranslation": "你好。我们来练习这个场景。你想先说什么？",
        "goals": ["explain_request", "use_polite_expression", "answer_follow_up_question"],
        "keywords": ["please", "help", "need", "question", "confirm"],
        "turns": [
            _turn(
                "custom-generic-turn-1",
                "explain_request",
                f"Thanks. In the {prompt} situation, what do you need first?",
                f"谢谢。在“{prompt}”这个场景里，你首先需要什么？",
            ),
            _turn(
                "custom-generic-turn-2",
                "answer_follow_up_question",
                f"Good. What detail about {prompt} should we confirm next?",
                f"很好。接下来我们要确认“{prompt}”的哪个细节？",
            ),
        ],
        "fallbackReplies": [f"Could you explain what you need in the {prompt} situation?"],
        "fallbackReplyTranslations": [f"请说明你在“{prompt}”这个场景里的需求。"],
    }


def _turn(turn_id: str, expected_intent: str, reply: str, reply_translation: str) -> Dict[str, str]:
    return {
        "id": turn_id,
        "expectedIntent": expected_intent,
        "reply": reply,
        "replyTranslation": reply_translation,
    }
