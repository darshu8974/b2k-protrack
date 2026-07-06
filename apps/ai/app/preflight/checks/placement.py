"""Placement check: live content should stay inside the safe margin from the trim edge."""

from __future__ import annotations

from app.parsers.models import PdfFacts
from app.preflight.models import CheckOutcome, Finding, page_ref
from app.schemas.common import CheckResult, Severity

KEY = "placement"
CATEGORY = "layout"
_SAFE_MARGIN_PT = 18.0  # ~0.25 inch


def check(facts: PdfFacts, standard: str | None) -> CheckOutcome:
    tight = [
        page.index
        for page in facts.pages
        if not page.content_overflow
        and page.min_margin_pt is not None
        and page.min_margin_pt < _SAFE_MARGIN_PT
    ]
    if tight:
        detail = (
            f"Content sits within {_SAFE_MARGIN_PT:.0f} pt of the trim edge "
            f"on {len(tight)} page(s)."
        )
        return CheckOutcome(
            KEY,
            CheckResult.REVIEW,
            detail,
            Finding(
                check_key=KEY,
                category=CATEGORY,
                result=CheckResult.REVIEW,
                page_ref=page_ref(tight),
                default_severity=Severity.MEDIUM,
                default_title="Content too close to trim edge",
                default_recommendation=(
                    "Keep live content inside the safe margin so it is not lost when trimmed."
                ),
                evidence=detail,
            ),
        )
    return CheckOutcome(
        KEY, CheckResult.PASS, "Content respects the safe margin on all measured pages."
    )
