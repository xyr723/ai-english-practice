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


def test_matched_goals_detects_ordering_and_politeness():
    scenario = load_scenario("restaurant")

    goals = matched_goals(scenario, "I'd like to order a coffee, please.")

    assert "order_food_or_drink" in goals
    assert "use_polite_expression" in goals

