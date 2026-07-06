"""Geometry check: page-size consistency and, for print standards, trim/bleed presence."""

from __future__ import annotations

from app.parsers.models import PdfFacts
from app.preflight.models import CheckOutcome, Finding
from app.schemas.common import CheckResult, Severity

KEY = "geometry"
CATEGORY = "layout"


def check(facts: PdfFacts, standard: str | None) -> CheckOutcome:
    pages = facts.pages
    if not pages:
        detail = "No page geometry could be read from the PDF."
        return CheckOutcome(
            KEY,
            CheckResult.REVIEW,
            detail,
            Finding(
                check_key=KEY,
                category=CATEGORY,
                result=CheckResult.REVIEW,
                page_ref=None,
                default_severity=Severity.MEDIUM,
                default_title="Page geometry unreadable",
                default_recommendation="Re-export the PDF so page dimensions can be verified.",
                evidence=detail,
            ),
        )

    sizes = {(page.width_pt, page.height_pt) for page in pages}
    if len(sizes) > 1:
        detail = f"Pages use {len(sizes)} different sizes; a title should share one trim size."
        return CheckOutcome(
            KEY,
            CheckResult.REVIEW,
            detail,
            Finding(
                check_key=KEY,
                category=CATEGORY,
                result=CheckResult.REVIEW,
                page_ref=None,
                default_severity=Severity.MEDIUM,
                default_title="Inconsistent page dimensions",
                default_recommendation=(
                    "Normalize all pages to a single trim size before production."
                ),
                evidence=detail,
            ),
        )

    print_standard = standard is not None and standard.upper().startswith("PDF/X")
    if print_standard and any(not (p.has_trimbox or p.has_bleedbox) for p in pages):
        detail = f"No trim/bleed boxes defined for the {standard} print standard."
        return CheckOutcome(
            KEY,
            CheckResult.REVIEW,
            detail,
            Finding(
                check_key=KEY,
                category=CATEGORY,
                result=CheckResult.REVIEW,
                page_ref=None,
                default_severity=Severity.MEDIUM,
                default_title="Missing trim/bleed boxes",
                default_recommendation=(
                    "Add trim and bleed boxes matching the target print standard."
                ),
                evidence=detail,
            ),
        )

    width, height = next(iter(sizes))
    return CheckOutcome(
        KEY,
        CheckResult.PASS,
        f"Uniform page size {width:g}×{height:g} pt across {len(pages)} page(s).",
    )
