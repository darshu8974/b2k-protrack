"""Merges deterministic parser facts with validated LLM judgement into the normalized
AnalysisResult that Spring Boot persists 1:1.

Counts (metrics) and heading counts always come from the parser — never the model. Composition,
risks, suggested team, complexity, summary, and confidence come from the LLM. Construction through
the Pydantic contract IS the validation step; malformed model output raises OutputValidationError.
"""

from __future__ import annotations

from pydantic import ValidationError

from app.core.errors import OutputValidationError
from app.parsers.models import ParsedDocument
from app.providers.base import LLMResponse
from app.schemas.analysis import (
    AnalysisResult,
    CompositionSegment,
    HeadingCount,
    Metric,
    RiskFlag,
    TeamSuggestion,
)
from app.schemas.internal import LLMUsage

# Deterministic parser counts carry full confidence.
_DETERMINISTIC_CONFIDENCE = 100
_HEADING_ORDER = ("H1", "H2", "H3")


class AnalysisNormalizer:
    def normalize(
        self,
        parsed: ParsedDocument,
        llm: LLMResponse,
        *,
        prompt_id: str | None = None,
        model: str | None = None,
    ) -> AnalysisResult:
        data = llm.data
        try:
            return AnalysisResult(
                overall_confidence=data["overallConfidence"],
                summary=data["summary"],
                language=data.get("language") or parsed.language,
                complexity_score=data["complexityScore"],
                complexity_label=data.get("complexityLabel"),
                estimated_working_days=data.get("estimatedWorkingDays"),
                metrics=_metrics(parsed),
                composition=[
                    CompositionSegment(segment=item["segment"], percentage=item["percentage"])
                    for item in data["composition"]
                ],
                headings=_headings(parsed),
                risks=[
                    RiskFlag(
                        severity=item["severity"],
                        title=item["title"],
                        description=item["description"],
                    )
                    for item in data["risks"]
                ],
                suggested_team=[
                    TeamSuggestion(
                        role=item["role"],
                        match_score=item["matchScore"],
                        rationale=item["rationale"],
                        candidate_hint=item.get("candidateHint"),
                    )
                    for item in data["suggestedTeam"]
                ],
                prompt_id=prompt_id,
                model=model or llm.usage.model,
                usage=LLMUsage(
                    input_tokens=llm.usage.input_tokens,
                    output_tokens=llm.usage.output_tokens,
                    model=llm.usage.model,
                ),
            )
        except (ValidationError, KeyError, TypeError) as exc:
            raise OutputValidationError(f"LLM output failed normalization: {exc}") from exc


def _metrics(parsed: ParsedDocument) -> list[Metric]:
    counts = parsed.counts
    return [
        Metric(key=key, value=value, confidence=_DETERMINISTIC_CONFIDENCE)
        for key, value in (
            ("pages", counts.pages),
            ("figures", counts.figures),
            ("tables", counts.tables),
            ("equations", counts.equations),
            ("problems", counts.problems),
            ("references", counts.references),
        )
    ]


def _headings(parsed: ParsedDocument) -> list[HeadingCount]:
    tally: dict[str, int] = {}
    for heading in parsed.headings:
        tally[heading.level] = tally.get(heading.level, 0) + 1

    def _rank(level: str) -> int:
        return _HEADING_ORDER.index(level) if level in _HEADING_ORDER else len(_HEADING_ORDER)

    ordered = sorted(tally.items(), key=lambda kv: _rank(kv[0]))
    return [HeadingCount(level=level, count=count) for level, count in ordered]
