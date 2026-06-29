"""Merges deterministic parser facts with validated LLM output into the normalized response
schemas that Spring Boot persists 1:1. Implemented in Sprint 4/5."""

from __future__ import annotations

from app.parsers.models import ParsedDocument
from app.providers.base import LLMResponse
from app.schemas.analysis import AnalysisResult


class AnalysisNormalizer:
    def normalize(self, parsed: ParsedDocument, llm: LLMResponse) -> AnalysisResult:
        raise NotImplementedError("Normalization is implemented in Sprint 4")
