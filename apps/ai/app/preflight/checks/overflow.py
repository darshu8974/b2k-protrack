"""Overflow check: no content should extend beyond the page boundary."""

from __future__ import annotations

from app.parsers.models import PdfFacts
from app.preflight.models import CheckOutcome, Finding, page_ref
from app.schemas.common import CheckResult, Severity

KEY = "overflow"
CATEGORY = "layout"


def check(facts: PdfFacts, standard: str | None) -> CheckOutcome:
    overflowing = [page.index for page in facts.pages if page.content_overflow]
    if overflowing:
        detail = f"Content extends beyond the page boundary on {len(overflowing)} page(s)."
        return CheckOutcome(
            KEY,
            CheckResult.FAIL,
            detail,
            Finding(
                check_key=KEY,
                category=CATEGORY,
                result=CheckResult.FAIL,
                page_ref=page_ref(overflowing),
                default_severity=Severity.HIGH,
                default_title="Content overflow",
                default_recommendation=(
                    "Reflow content within the trim area; nothing should exceed the page edge."
                ),
                evidence=detail,
            ),
        )
    return CheckOutcome(KEY, CheckResult.PASS, "No content extends beyond the page boundary.")
