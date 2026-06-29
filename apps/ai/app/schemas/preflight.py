"""PDF-preflight request/response contract (maps to preflight_runs / preflight_checks / qa_issues)."""

from __future__ import annotations

from pydantic import BaseModel

from app.schemas.common import CheckResult, Confidence, Severity
from app.schemas.internal import LLMUsage


class PdfPreflightRequest(BaseModel):
    job_id: str
    file_url: str
    standard: str | None = None


class PreflightCheck(BaseModel):
    key: str
    result: CheckResult
    detail: str | None = None


class PreflightIssue(BaseModel):
    category: str
    severity: Severity
    title: str
    recommendation: str
    page_ref: str | None = None
    source: str = "AI"


class PreflightTotals(BaseModel):
    issues: int = 0
    high: int = 0


class PreflightResult(BaseModel):
    overall_score: Confidence
    passed: bool
    standard: str | None = None
    checks: list[PreflightCheck] = []
    issues: list[PreflightIssue] = []
    totals: PreflightTotals = PreflightTotals()
    prompt_id: str | None = None
    model: str | None = None
    usage: LLMUsage | None = None
