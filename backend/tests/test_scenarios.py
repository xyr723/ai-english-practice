from app.services.scenarios import load_scenario, matched_goals, next_reply


def test_load_scenario_returns_restaurant_metadata():
    scenario = load_scenario("restaurant")

    assert scenario["id"] == "restaurant"
    assert scenario["name"] == "Restaurant Ordering"
    assert scenario["role"] == "Waitress"
    assert scenario["turns"]


def test_next_reply_uses_scripted_turn_order():
    scenario = load_scenario("restaurant")

    assert (
        next_reply(scenario, "I'd like to order a coffee, please.", turn_index=0)
        == "Sure. Would you like anything to drink?"
    )


def test_next_reply_does_not_repeat_say_that_again_after_scripted_turns():
    scenario = load_scenario("restaurant")

    assert (
        next_reply(scenario, "Thank you.", turn_index=len(scenario["turns"]))
        == "Great. That completes this practice. Try one more answer with a specific detail."
    )


def test_matched_goals_detects_ordering_and_politeness():
    scenario = load_scenario("restaurant")

    goals = matched_goals(scenario, "I'd like to order a coffee, please.")

    assert "order_food_or_drink" in goals
    assert "use_polite_expression" in goals


def test_load_scenario_generates_android_builtin_scenarios_when_json_is_missing():
    interview = load_scenario("interview")
    meeting = load_scenario("meeting")

    assert interview["id"] == "interview"
    assert interview["turns"][0]["reply"] == "Good. Could you tell me about one project you worked on?"
    assert meeting["id"] == "meeting"
    assert "give_opinion" in meeting["goals"]


def test_load_scenario_generates_custom_airport_scene_from_android_id():
    scenario = load_scenario("custom-airport-机场改签")

    assert scenario["id"] == "custom-airport-机场改签"
    assert scenario["role"] == "Airline staff"
    assert "flight" in scenario["keywords"]
    assert next_reply(scenario, "I need to change my flight, please.", turn_index=0) == (
        "Sure. Which flight would you like to change?"
    )


def test_load_scenario_generates_unknown_custom_scene_without_404():
    scenario = load_scenario("custom-custom-健身房办卡")

    assert scenario["id"] == "custom-custom-健身房办卡"
    assert "健身房办卡" in scenario["name"]
    assert "健身房办卡" in scenario["turns"][0]["reply"]
