from app.services.correction import check_text


def test_check_text_rewrites_common_ordering_mistake_and_adds_politeness():
    result = check_text(
        "I want order a coffee",
        scenario_keywords=["coffee", "tea", "sandwich"],
        require_polite=True,
    )

    assert result["original"] == "I want order a coffee"
    assert result["betterExpression"] == "I'd like to order a coffee, please."
    assert result["source"] == "RULE_ONLY"
    assert {issue["type"] for issue in result["issues"]} == {
        "grammar",
        "politeness",
    }


def test_check_text_keeps_polite_correct_expression_stable():
    result = check_text(
        "I'd like to order a coffee, please.",
        scenario_keywords=["coffee"],
        require_polite=True,
    )

    assert result["betterExpression"] == "I'd like to order a coffee, please."
    assert result["issues"] == []

