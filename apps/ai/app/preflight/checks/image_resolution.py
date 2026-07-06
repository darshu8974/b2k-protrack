"""Image-resolution check: raster images should be at or above print resolution (300 DPI)."""

from __future__ import annotations

from app.parsers.models import PdfFacts
from app.preflight.models import CheckOutcome, Finding, page_ref
from app.schemas.common import CheckResult, Severity

KEY = "image_resolution"
CATEGORY = "images"
_FAIL_DPI = 150
_REVIEW_DPI = 300


def check(facts: PdfFacts, standard: str | None) -> CheckOutcome:
    if not facts.images:
        return CheckOutcome(KEY, CheckResult.PASS, "No raster images to check.")

    measured = [(image.page_index, image.dpi) for image in facts.images if image.dpi is not None]
    if not measured:
        return CheckOutcome(
            KEY,
            CheckResult.PASS,
            f"{len(facts.images)} image(s) present; resolution could not be measured.",
        )

    low = [(page, dpi) for page, dpi in measured if dpi < _FAIL_DPI]
    borderline = [(page, dpi) for page, dpi in measured if _FAIL_DPI <= dpi < _REVIEW_DPI]

    if low:
        lowest = min(dpi for _, dpi in low)
        detail = f"{len(low)} image(s) below {_FAIL_DPI} DPI (lowest {lowest:.0f} DPI)."
        return CheckOutcome(
            KEY,
            CheckResult.FAIL,
            detail,
            Finding(
                check_key=KEY,
                category=CATEGORY,
                result=CheckResult.FAIL,
                page_ref=page_ref([page for page, _ in low]),
                default_severity=Severity.HIGH,
                default_title="Low-resolution images",
                default_recommendation=(
                    f"Replace images below {_REVIEW_DPI} DPI with print-resolution assets."
                ),
                evidence=detail,
            ),
        )

    if borderline:
        detail = f"{len(borderline)} image(s) between {_FAIL_DPI} and {_REVIEW_DPI} DPI."
        return CheckOutcome(
            KEY,
            CheckResult.REVIEW,
            detail,
            Finding(
                check_key=KEY,
                category=CATEGORY,
                result=CheckResult.REVIEW,
                page_ref=page_ref([page for page, _ in borderline]),
                default_severity=Severity.MEDIUM,
                default_title="Borderline image resolution",
                default_recommendation=f"Review images under {_REVIEW_DPI} DPI for print quality.",
                evidence=detail,
            ),
        )

    return CheckOutcome(
        KEY,
        CheckResult.PASS,
        f"All {len(measured)} measured image(s) at or above {_REVIEW_DPI} DPI.",
    )
