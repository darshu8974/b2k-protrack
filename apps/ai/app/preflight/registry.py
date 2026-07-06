"""Registry of the Phase-1 lightweight preflight checks.

The tuple order defines both the UI checklist order and the persisted
``preflight_checks.sort_order`` (Spring assigns it from the array order). Each check is a
deterministic ``CheckFn`` producing a
``CheckOutcome``; new checks register here with no orchestration change.
"""

from __future__ import annotations

from collections.abc import Callable

from app.parsers.models import PdfFacts
from app.preflight.checks import (
    accessibility,
    font_embedding,
    geometry,
    image_resolution,
    overflow,
    placement,
)
from app.preflight.models import CheckOutcome

CheckFn = Callable[[PdfFacts, str | None], CheckOutcome]

CHECKS: tuple[tuple[str, CheckFn], ...] = (
    (geometry.KEY, geometry.check),
    (font_embedding.KEY, font_embedding.check),
    (image_resolution.KEY, image_resolution.check),
    (overflow.KEY, overflow.check),
    (placement.KEY, placement.check),
    (accessibility.KEY, accessibility.check),
)

# Ordered check keys surfaced in the UI checklist (see PreflightCheck.key).
CHECK_KEYS: tuple[str, ...] = tuple(key for key, _ in CHECKS)
