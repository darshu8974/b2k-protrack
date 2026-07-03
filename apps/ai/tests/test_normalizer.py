"""Normalizer tests: parser facts + LLM judgement -> validated AnalysisResult."""

from __future__ import annotations

import pytest

from app.core.errors import OutputValidationError
from app.parsers.models import Counts, Heading, ParsedDocument
from app.providers.base import LLMResponse, Usage
from app.services.normalizer import AnalysisNormalizer

_LLM_DATA = {
    "overallConfidence": 84,
    "summary": "Solid manuscript.",
    "language": None,
    "complexityScore": 62,
    "complexityLabel": "Moderate",
    "estimatedWorkingDays": 18,
    "composition": [{"segment": "Body text", "percentage": 70.0}],
    "risks": [{"severity": "MEDIUM", "title": "Notation", "description": "Mixed notation."}],
    "suggestedTeam": [{"role": "Copyeditor", "matchScore": 88, "rationale": "STEM experience."}],
}


def _parsed() -> ParsedDocument:
    return ParsedDocument(
        counts=Counts(pages=120, figures=8, tables=5, equations=42, problems=30, references=60),
        headings=[Heading(level="H1", text="Intro"), Heading(level="H2", text="Methods"),
                  Heading(level="H1", text="Refs")],
        language="en",
    )


def test_metrics_come_from_parser_headings_aggregated() -> None:
    result = AnalysisNormalizer().normalize(
        _parsed(), LLMResponse(data=_LLM_DATA, usage=Usage(model="mock")), prompt_id="p.v1"
    )

    metrics = {m.key: m for m in result.metrics}
    assert metrics["pages"].value == 120
    assert metrics["equations"].value == 42
    assert all(m.confidence == 100 for m in result.metrics)  # deterministic counts

    heading_counts = {h.level: h.count for h in result.headings}
    assert heading_counts == {"H1": 2, "H2": 1}

    # Judgement comes from the LLM; language falls back to the parser when LLM gives null.
    assert result.overall_confidence == 84
    assert result.composition[0].segment == "Body text"
    assert result.risks[0].severity.value == "MEDIUM"
    assert result.language == "en"
    assert result.prompt_id == "p.v1"


def test_serialized_result_is_camel_case() -> None:
    result = AnalysisNormalizer().normalize(
        _parsed(), LLMResponse(data=_LLM_DATA, usage=Usage(model="mock")), prompt_id="p.v1"
    )
    dumped = result.model_dump(by_alias=True)
    assert "overallConfidence" in dumped
    assert "suggestedTeam" in dumped
    assert dumped["metrics"][0]["confidence"] == 100


def test_invalid_llm_output_raises_validation_error() -> None:
    broken = {**_LLM_DATA, "overallConfidence": 999}  # out of range -> Confidence fails
    with pytest.raises(OutputValidationError):
        AnalysisNormalizer().normalize(
            _parsed(), LLMResponse(data=broken, usage=Usage(model="mock")), prompt_id="p.v1"
        )
