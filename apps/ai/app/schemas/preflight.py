"""PDF-preflight request/response contract (maps 1:1 to the preflight_runs / preflight_checks /
qa_issues DB tables).

Wire form is camelCase (``CamelModel``), matching the API specification and Spring's Jackson
mapping — the same convention as the analysis contract. Deterministic checks own ``result`` and the
overall score; the LLM only phrases each issue's severity, title and recommendation.
"""

from __future__ import annotations

from app.schemas.common import CamelModel, CheckResult, Confidence, Severity
from app.schemas.internal import LLMUsage


class PdfPreflightRequest(CamelModel):
    job_id: str
    file_url: str
    standard: str | None = None


class PreflightCheck(CamelModel):
    key: str
    result: CheckResult
    detail: str | None = None


class PreflightIssue(CamelModel):
    category: str
    severity: Severity
    title: str
    recommendation: str
    page_ref: str | None = None
    source: str = "AI"


class PreflightTotals(CamelModel):
    issues: int = 0
    high: int = 0


class PreflightResult(CamelModel):
    overall_score: Confidence
    passed: bool
    standard: str | None = None
    checks: list[PreflightCheck] = []
    issues: list[PreflightIssue] = []
    totals: PreflightTotals = PreflightTotals()
    prompt_id: str | None = None
    model: str | None = None
    usage: LLMUsage | None = None
