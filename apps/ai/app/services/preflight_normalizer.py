"""Merges deterministic check outcomes with the LLM's phrasing into the normalized PreflightResult
that Spring Boot persists 1:1.

Deterministic checks are the source of truth: ``checks[]``, the overall score, pass/fail, and each
issue's category/pageRef come from the runner. The LLM only refines each finding's severity, title
and recommendation (bound by check_key). Invalid or missing phrasing falls back to the deterministic
defaults — the LLM can never add, drop, or invalidate a finding.
"""

from __future__ import annotations

from typing import Any

from pydantic import ValidationError

from app.core.errors import OutputValidationError
from app.preflight.models import CheckOutcome
from app.preflight.scoring import score_and_pass
from app.providers.base import LLMResponse
from app.schemas.common import Severity
from app.schemas.internal import LLMUsage
from app.schemas.preflight import (
    PreflightCheck,
    PreflightIssue,
    PreflightResult,
    PreflightTotals,
)

_VALID_SEVERITIES = {member.value for member in Severity}


class PreflightNormalizer:
    def normalize(
        self,
        outcomes: list[CheckOutcome],
        llm: LLMResponse,
        *,
        standard: str | None = None,
        prompt_id: str | None = None,
        model: str | None = None,
    ) -> PreflightResult:
        phrasings = _phrasings_by_key(llm.data)
        checks = [
            PreflightCheck(key=outcome.key, result=outcome.result, detail=outcome.detail)
            for outcome in outcomes
        ]
        issues: list[PreflightIssue] = []
        for outcome in outcomes:
            finding = outcome.finding
            if finding is None:
                continue
            phrasing = phrasings.get(finding.check_key, {})
            issues.append(
                PreflightIssue(
                    category=finding.category,
                    severity=_severity(phrasing) or finding.default_severity,
                    title=_text(phrasing, "title") or finding.default_title,
                    recommendation=(
                        _text(phrasing, "recommendation") or finding.default_recommendation
                    ),
                    page_ref=finding.page_ref,
                    source="AI",
                )
            )

        score, passed = score_and_pass(outcomes)
        totals = PreflightTotals(
            issues=len(issues),
            high=sum(1 for issue in issues if issue.severity is Severity.HIGH),
        )
        try:
            return PreflightResult(
                overall_score=score,
                passed=passed,
                standard=standard,
                checks=checks,
                issues=issues,
                totals=totals,
                prompt_id=prompt_id,
                model=model or llm.usage.model,
                usage=LLMUsage(
                    input_tokens=llm.usage.input_tokens,
                    output_tokens=llm.usage.output_tokens,
                    model=llm.usage.model,
                ),
            )
        except (ValidationError, KeyError, TypeError) as exc:
            raise OutputValidationError(f"Preflight result failed normalization: {exc}") from exc


def _phrasings_by_key(data: dict[str, Any]) -> dict[str, dict[str, Any]]:
    result: dict[str, dict[str, Any]] = {}
    for item in data.get("issues", []) or []:
        if isinstance(item, dict) and isinstance(item.get("checkKey"), str):
            result[item["checkKey"]] = item
    return result


def _severity(phrasing: dict[str, Any]) -> Severity | None:
    value = phrasing.get("severity")
    return Severity(value) if value in _VALID_SEVERITIES else None


def _text(phrasing: dict[str, Any], key: str) -> str | None:
    value = phrasing.get(key)
    return value.strip() if isinstance(value, str) and value.strip() else None
