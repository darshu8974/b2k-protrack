"""Deterministic preflight scoring.

The overall score and pass/fail are computed from the check results only — never from the LLM — so
the QA quality signal reflects the deterministic facts. Each REVIEW/FAIL deducts a fixed penalty; a
run passes when the score meets the threshold.
"""

from __future__ import annotations

from collections.abc import Iterable

from app.preflight.models import CheckOutcome
from app.schemas.common import CheckResult

_REVIEW_PENALTY = 10
_FAIL_PENALTY = 34
PASS_THRESHOLD = 70


def score_and_pass(outcomes: Iterable[CheckOutcome]) -> tuple[int, bool]:
    score = 100
    for outcome in outcomes:
        if outcome.result is CheckResult.FAIL:
            score -= _FAIL_PENALTY
        elif outcome.result is CheckResult.REVIEW:
            score -= _REVIEW_PENALTY
    score = max(0, min(100, score))
    return score, score >= PASS_THRESHOLD
