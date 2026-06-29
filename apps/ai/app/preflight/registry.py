"""Registry of Phase-1 lightweight preflight checks. Check functions are implemented in Sprint 5."""

from __future__ import annotations

# Ordered check keys surfaced in the UI checklist (see PreflightCheck.key).
CHECK_KEYS: tuple[str, ...] = (
    "geometry",
    "font_embedding",
    "image_resolution",
    "overflow",
    "placement",
    "accessibility",
)
