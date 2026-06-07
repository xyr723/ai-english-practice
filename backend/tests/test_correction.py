from app.services.correction import (
    DEFAULT_LANGUAGE_TOOL_TIMEOUT_SECONDS,
    LanguageToolClient,
    LanguageToolUnavailable,
    check_text,
    create_language_tool_checker,
)


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


def test_check_text_fixes_common_verb_agreement_without_politeness_only():
    result = check_text("I has a question")

    assert result["betterExpression"] == "I have a question."
    assert {issue["type"] for issue in result["issues"]} == {"grammar"}


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


def test_check_text_adds_airport_scenario_guidance_beyond_please():
    result = check_text(
        "I need to change my flight please",
        scenario_id="custom-airport-机场改签",
        scenario_keywords=["flight", "change", "ticket", "time", "please", "rebook"],
    )

    assert result["source"] == "RULE_ONLY"
    assert "flight number" in result["betterExpression"]
    assert any("flight number" in issue["message"] for issue in result["issues"])
    assert any(issue["type"] == "scenario" for issue in result["issues"])


def test_check_text_adds_interview_scenario_guidance():
    result = check_text(
        "I worked on a project",
        scenario_id="interview",
        scenario_keywords=["experience", "project", "team", "strength", "learn"],
    )

    assert "STAR" in result["betterExpression"]
    assert any("STAR" in issue["message"] for issue in result["issues"])


def test_check_text_adds_meeting_scenario_guidance():
    result = check_text(
        "I think we should change the plan",
        scenario_id="meeting",
        scenario_keywords=["timeline", "risk", "plan", "next step", "agree"],
    )

    assert "risk" in result["betterExpression"].lower()
    assert any("risk" in issue["message"].lower() for issue in result["issues"])


def test_language_tool_default_timeout_allows_slow_cloud_checks():
    assert DEFAULT_LANGUAGE_TOOL_TIMEOUT_SECONDS >= 3.0


def test_create_language_tool_checker_uses_public_api_by_default(monkeypatch):
    monkeypatch.delenv("LANGUAGETOOL_URL", raising=False)

    checker = create_language_tool_checker()

    assert isinstance(checker, LanguageToolClient)
    assert checker.url == "https://api.languagetool.org/v2/check"


def test_create_language_tool_checker_prefers_configured_url(monkeypatch):
    monkeypatch.setenv("LANGUAGETOOL_URL", "http://127.0.0.1:8081/v2/check")

    checker = create_language_tool_checker()

    assert isinstance(checker, LanguageToolClient)
    assert checker.url == "http://127.0.0.1:8081/v2/check"
