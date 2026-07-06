"""Accessibility check (lightweight): tagged structure + a document language.

Phase-1 advisory only — accessibility gaps are surfaced as REVIEW, not a hard print failure.
"""

from __future__ import annotations

from app.parsers.models import PdfFacts
from app.preflight.models import CheckOutcome, Finding
from app.schemas.common import CheckResult, Severity

KEY = "accessibility"
CATEGORY = "accessibility"


def check(facts: PdfFacts, standard: str | None) -> CheckOutcome:
    access = facts.accessibility
    if not access.tagged:
        detail = "PDF is not tagged (no structure tree); reading order cannot be determined."
        return CheckOutcome(
            KEY,
            CheckResult.REVIEW,
            detail,
            Finding(
                check_key=KEY,
                category=CATEGORY,
                result=CheckResult.REVIEW,
                page_ref=None,
                default_severity=Severity.LOW,
                default_title="Untagged PDF",
                default_recommendation=(
                    "Export a tagged PDF with a document language for accessibility."
                ),
                evidence=detail,
            ),
        )
    if not access.has_lang:
        detail = "Tagged PDF has no document language set."
        return CheckOutcome(
            KEY,
            CheckResult.REVIEW,
            detail,
            Finding(
                check_key=KEY,
                category=CATEGORY,
                result=CheckResult.REVIEW,
                page_ref=None,
                default_severity=Severity.LOW,
                default_title="Missing document language",
                default_recommendation="Set the document language in the PDF metadata.",
                evidence=detail,
            ),
        )
    return CheckOutcome(KEY, CheckResult.PASS, "PDF is tagged with a document language.")
