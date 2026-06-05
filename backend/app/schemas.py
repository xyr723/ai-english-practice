from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class GrammarCheckRequest(BaseModel):
    text: str
    language: str = "en-US"
    scenarioId: Optional[str] = None


class CoachAnalyzeRequest(BaseModel):
    scenarioId: str
    turnText: str
    history: List[Dict[str, Any]] = Field(default_factory=list)
    durationMs: int = 0
    asrConfidence: Optional[float] = None
    turnIndex: int = 0


class SummaryRequest(BaseModel):
    scenarioId: str
    turns: List[Dict[str, Any]] = Field(default_factory=list)

