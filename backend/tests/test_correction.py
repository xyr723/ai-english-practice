from app.services.correction import LanguageToolUnavailable, check_text


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


def test_check_text_uses_language_tool_when_checker_succeeds():
    def fake_checker(text, language):
        assert text == "I has coffee."
        assert language == "en-US"
        return [
            {
                "message": "Possible agreement error.",
                "offset": 2,
                "length": 3,
                "replacements": [{"value": "have"}],
                "rule": {"category": {"id": "GRAMMAR"}},
            }
        ]

    result = check_text("I has coffee", language_tool_checker=fake_checker)

    assert result["betterExpression"] == "I have coffee."
    assert result["source"] == "LANGUAGE_TOOL"
    assert result["issues"] == [
        {
            "type": "grammar",
            "message": "Possible agreement error.",
            "suggestion": "have",
        }
    ]


def test_check_text_falls_back_to_rules_when_language_tool_times_out():
    def timeout_checker(text, language):
        raise LanguageToolUnavailable("LanguageTool timed out")

    result = check_text(
        "I want order a coffee",
        scenario_keywords=["coffee"],
        require_polite=True,
        language_tool_checker=timeout_checker,
    )

    assert result["betterExpression"] == "I'd like to order a coffee, please."
    assert result["source"] == "RULE_FALLBACK"
    assert {issue["type"] for issue in result["issues"]} == {
        "grammar",
        "politeness",
    }
