from fastapi import FastAPI, HTTPException

from app.schemas import CoachAnalyzeRequest, GrammarCheckRequest, SummaryRequest
from app.services.correction import check_text, create_language_tool_checker
from app.services.llm import create_deepseek_coach_client
from app.services.scenarios import (
    list_scenarios,
    load_scenario,
    matched_goals,
    next_reply,
    next_reply_translation,
)
from app.services.scoring import score_turn
from app.services.summary import summarize_practice

app = FastAPI(title="AI English Practice API")
LANGUAGE_TOOL_CHECKER = create_language_tool_checker()
DEEPSEEK_COACH_CLIENT = create_deepseek_coach_client()


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/scenarios")
def scenarios():
    return {"items": list_scenarios()}


@app.post("/grammar/check")
def grammar_check(request: GrammarCheckRequest):
    scenario_keywords = []
    require_polite = False
    if request.scenarioId:
        scenario = load_scenario(request.scenarioId)
        scenario_keywords = scenario.get("keywords", [])
        require_polite = "use_polite_expression" in scenario.get("goals", [])

    return check_text(
        request.text,
        scenario_id=request.scenarioId or "",
        scenario_keywords=scenario_keywords,
        require_polite=require_polite,
        language=request.language,
        language_tool_checker=LANGUAGE_TOOL_CHECKER,
    )


@app.post("/coach/analyze")
def coach_analyze(request: CoachAnalyzeRequest):
    try:
        scenario = load_scenario(request.scenarioId)
    except FileNotFoundError as exc:
        raise HTTPException(status_code=404, detail="Scenario not found") from exc

    fallback = _rule_based_analysis(request, scenario)
    if DEEPSEEK_COACH_CLIENT is None:
        return fallback

    try:
        deepseek_result = DEEPSEEK_COACH_CLIENT.analyze(scenario, request, fallback)
    except Exception:
        return fallback

    return {
        **fallback,
        **deepseek_result,
        "source": "DEEPSEEK",
    }


@app.post("/summary")
def summary(request: SummaryRequest):
    try:
        scenario = load_scenario(request.scenarioId)
    except FileNotFoundError as exc:
        raise HTTPException(status_code=404, detail="Scenario not found") from exc

    fallback = summarize_practice(request.turns, scenario=scenario)
    if DEEPSEEK_COACH_CLIENT is None:
        return fallback

    try:
        deepseek_result = DEEPSEEK_COACH_CLIENT.summarize(scenario, request, fallback)
    except Exception:
        return {**fallback, "source": "RULE_FALLBACK"}

    return {
        **fallback,
        **deepseek_result,
        "source": "DEEPSEEK",
    }


def _rule_based_analysis(request: CoachAnalyzeRequest, scenario):
    correction = check_text(
        request.turnText,
        scenario_id=request.scenarioId,
        scenario_keywords=scenario.get("keywords", []),
        require_polite="use_polite_expression" in scenario.get("goals", []),
        language_tool_checker=LANGUAGE_TOOL_CHECKER,
    )
    goals = matched_goals(scenario, request.turnText)
    grammar_issue_count, spelling_issue_count = _issue_counts(correction["issues"])
    scores = score_turn(
        word_count=len(request.turnText.split()),
        duration_ms=request.durationMs,
        asr_confidence=request.asrConfidence,
        grammar_issue_count=grammar_issue_count,
        spelling_issue_count=spelling_issue_count,
        matched_goals=len(goals),
        total_goals=len(scenario.get("goals", [])),
        recognition_alternatives=request.recognitionAlternatives,
    )

    return {
        "reply": next_reply(scenario, request.turnText, request.turnIndex),
        "replyTranslation": next_reply_translation(scenario, request.turnIndex, request.turnText),
        "betterExpression": correction["betterExpression"],
        "tips": [issue["message"] for issue in correction["issues"]],
        "scores": scores,
        "source": correction["source"],
    }


def _issue_counts(issues):
    spelling_issue_count = sum(
        1 for issue in issues if isinstance(issue, dict) and issue.get("type") == "spelling"
    )
    grammar_issue_count = sum(
        1
        for issue in issues
        if isinstance(issue, dict) and issue.get("type") not in ("scenario", "spelling")
    )
    return grammar_issue_count, spelling_issue_count
