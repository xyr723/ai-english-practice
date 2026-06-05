from fastapi import FastAPI, HTTPException

from app.schemas import CoachAnalyzeRequest, GrammarCheckRequest, SummaryRequest
from app.services.correction import check_text
from app.services.scenarios import list_scenarios, load_scenario, matched_goals, next_reply
from app.services.scoring import score_turn
from app.services.summary import summarize_practice

app = FastAPI(title="AI English Practice API")


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
        scenario_keywords=scenario_keywords,
        require_polite=require_polite,
    )


@app.post("/coach/analyze")
def coach_analyze(request: CoachAnalyzeRequest):
    try:
        scenario = load_scenario(request.scenarioId)
    except FileNotFoundError as exc:
        raise HTTPException(status_code=404, detail="Scenario not found") from exc

    correction = check_text(
        request.turnText,
        scenario_keywords=scenario.get("keywords", []),
        require_polite="use_polite_expression" in scenario.get("goals", []),
    )
    goals = matched_goals(scenario, request.turnText)
    scores = score_turn(
        word_count=len(request.turnText.split()),
        duration_ms=request.durationMs,
        asr_confidence=request.asrConfidence,
        grammar_issue_count=len(correction["issues"]),
        spelling_issue_count=0,
        matched_goals=len(goals),
        total_goals=len(scenario.get("goals", [])),
    )

    return {
        "reply": next_reply(scenario, request.turnText, request.turnIndex),
        "betterExpression": correction["betterExpression"],
        "tips": [issue["message"] for issue in correction["issues"]],
        "scores": scores,
        "source": correction["source"],
    }


@app.post("/summary")
def summary(request: SummaryRequest):
    return summarize_practice(request.turns)

