"""Deterministic mock LLM provider.

Returns fixed, schema-valid structured judgement so the entire pipeline runs end-to-end with no
external dependency or API key. Given the same request it always returns the same shape and values —
ideal for local development, CI, and demos. Selected by default (AI_PROVIDER=mock); swap to the real
Claude provider with AI_PROVIDER=claude.

The task is inferred from the requested ``output_schema`` (manuscript analysis vs. preflight
phrasing) so the single mock serves both verticals; the provider abstraction is unchanged.
"""

from __future__ import annotations

from typing import Any

from app.providers.base import LLMResponse, Usage

# A stable, representative manuscript-analysis judgement. Counts/headings are supplied by the
# deterministic parser and merged by the normalizer — this payload is judgement only.
_MOCK_JUDGEMENT: dict[str, Any] = {
    "overallConfidence": 84,
    "summary": (
        "The manuscript is well structured and production-ready with moderate effort. "
        "Notation and figure treatment are the main areas to watch."
    ),
    "language": "en",
    "complexityScore": 62,
    "complexityLabel": "Moderate",
    "estimatedWorkingDays": 18,
    "composition": [
        {"segment": "Body text", "percentage": 68.0},
        {"segment": "Equations", "percentage": 12.0},
        {"segment": "Figures", "percentage": 10.0},
        {"segment": "Tables", "percentage": 6.0},
        {"segment": "Problem sets", "percentage": 4.0},
    ],
    "risks": [
        {
            "severity": "MEDIUM",
            "title": "Notation consistency",
            "description": (
                "Mixed notation for vectors and matrices across chapters may need alignment."
            ),
        },
        {
            "severity": "LOW",
            "title": "Reference formatting",
            "description": (
                "Bibliography entries use inconsistent styles; normalize before typesetting."
            ),
        },
    ],
    "suggestedTeam": [
        {
            "role": "Copyeditor",
            "matchScore": 88,
            "rationale": "Strong track record on STEM notation and dense technical prose.",
            "candidateHint": None,
        },
        {
            "role": "Math typesetter",
            "matchScore": 82,
            "rationale": "Deep LaTeX/equation layout experience for heavy equation content.",
            "candidateHint": None,
        },
    ],
}


# Representative preflight phrasing, keyed by check so the normalizer overlays it onto whichever
# findings the deterministic checks actually raised. The checks decide which issues exist; this only
# supplies severity/title/recommendation text.
_MOCK_PREFLIGHT: dict[str, Any] = {
    "issues": [
        {
            "checkKey": "geometry",
            "severity": "MEDIUM",
            "title": "Page geometry needs review",
            "recommendation": "Confirm a single trim size and add trim/bleed for the standard.",
        },
        {
            "checkKey": "font_embedding",
            "severity": "HIGH",
            "title": "Embed all fonts",
            "recommendation": "Embed or outline every font so text renders correctly in print.",
        },
        {
            "checkKey": "image_resolution",
            "severity": "MEDIUM",
            "title": "Raise image resolution",
            "recommendation": "Replace low-resolution images with 300 DPI print assets.",
        },
        {
            "checkKey": "overflow",
            "severity": "HIGH",
            "title": "Content beyond page edge",
            "recommendation": "Reflow overflowing content back inside the trim area.",
        },
        {
            "checkKey": "placement",
            "severity": "MEDIUM",
            "title": "Content near trim edge",
            "recommendation": "Move live content inside the safe margin to survive trimming.",
        },
        {
            "checkKey": "accessibility",
            "severity": "LOW",
            "title": "Improve accessibility",
            "recommendation": "Export a tagged PDF with a document language set.",
        },
    ]
}


class MockProvider:
    """LLMProvider implementation returning deterministic, schema-valid data."""

    name = "mock"

    async def generate_structured(
        self,
        *,
        system: str,
        user: str,
        output_schema: dict[str, Any],
        options: dict[str, Any] | None = None,
    ) -> LLMResponse:
        # Deterministic: identical input -> identical output. A copy guards callers from mutation.
        payload = _MOCK_PREFLIGHT if _is_preflight_schema(output_schema) else _MOCK_JUDGEMENT
        data = {key: _clone(value) for key, value in payload.items()}
        usage = Usage(input_tokens=0, output_tokens=0, model=self.name)
        return LLMResponse(data=data, usage=usage, raw={"provider": "mock"})


def _is_preflight_schema(output_schema: dict[str, Any]) -> bool:
    """The preflight phrasing schema is distinguished by its top-level ``issues`` property."""
    properties = output_schema.get("properties", {}) if isinstance(output_schema, dict) else {}
    return "issues" in properties


def _clone(value: Any) -> Any:
    if isinstance(value, list):
        return [_clone(item) for item in value]
    if isinstance(value, dict):
        return {key: _clone(item) for key, item in value.items()}
    return value
