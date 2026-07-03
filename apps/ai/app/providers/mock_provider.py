"""Deterministic mock LLM provider.

Returns fixed, schema-valid structured judgement so the entire analysis pipeline runs
end-to-end with no external dependency or API key. Given the same request it always returns
the same shape and values — ideal for local development, CI, and demos. Selected by default
(AI_PROVIDER=mock); swap to the real Claude provider with AI_PROVIDER=claude.
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
        data = {key: _clone(value) for key, value in _MOCK_JUDGEMENT.items()}
        usage = Usage(input_tokens=0, output_tokens=0, model=self.name)
        return LLMResponse(data=data, usage=usage, raw={"provider": "mock"})


def _clone(value: Any) -> Any:
    if isinstance(value, list):
        return [_clone(item) for item in value]
    if isinstance(value, dict):
        return {key: _clone(item) for key, item in value.items()}
    return value
