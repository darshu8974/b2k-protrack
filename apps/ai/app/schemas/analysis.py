"""Manuscript-analysis request/response contract (maps 1:1 to the analysis_* DB tables)."""

from __future__ import annotations

from app.schemas.common import CamelModel, Confidence, Severity
from app.schemas.internal import LLMUsage, ProjectContext


class ManuscriptAnalysisRequest(CamelModel):
    job_id: str
    file_url: str
    doc_type: str
    project_context: ProjectContext | None = None


class Metric(CamelModel):
    key: str
    value: int
    confidence: Confidence


class CompositionSegment(CamelModel):
    segment: str
    percentage: float


class HeadingCount(CamelModel):
    level: str
    count: int


class RiskFlag(CamelModel):
    severity: Severity
    title: str
    description: str


class TeamSuggestion(CamelModel):
    role: str
    match_score: Confidence
    rationale: str
    candidate_hint: str | None = None


class AnalysisResult(CamelModel):
    overall_confidence: Confidence
    summary: str
    language: str | None = None
    complexity_score: Confidence
    complexity_label: str | None = None
    estimated_working_days: int | None = None
    metrics: list[Metric] = []
    composition: list[CompositionSegment] = []
    headings: list[HeadingCount] = []
    risks: list[RiskFlag] = []
    suggested_team: list[TeamSuggestion] = []
    prompt_id: str | None = None
    model: str | None = None
    usage: LLMUsage | None = None
