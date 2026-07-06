"""Font-embedding check: every text font must be embedded for reliable print output."""

from __future__ import annotations

from app.parsers.models import PdfFacts
from app.preflight.models import CheckOutcome, Finding
from app.schemas.common import CheckResult, Severity

KEY = "font_embedding"
CATEGORY = "fonts"
_MAX_NAMES = 8


def check(facts: PdfFacts, standard: str | None) -> CheckOutcome:
    fonts = facts.fonts
    if not fonts:
        return CheckOutcome(
            KEY, CheckResult.PASS, "No embedded-font issues: the document declares no text fonts."
        )

    non_embedded = sorted(font.name for font in fonts if not font.embedded)
    if non_embedded:
        names = ", ".join(non_embedded[:_MAX_NAMES])
        detail = f"{len(non_embedded)} font(s) not embedded: {names}."
        return CheckOutcome(
            KEY,
            CheckResult.FAIL,
            detail,
            Finding(
                check_key=KEY,
                category=CATEGORY,
                result=CheckResult.FAIL,
                page_ref=None,
                default_severity=Severity.HIGH,
                default_title="Fonts not embedded",
                default_recommendation=(
                    "Embed or outline all fonts so text renders identically at the print house."
                ),
                evidence=detail,
            ),
        )

    return CheckOutcome(KEY, CheckResult.PASS, f"All {len(fonts)} font(s) embedded.")
