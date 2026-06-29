"""Orchestrates manuscript analysis: load -> parse -> prompt -> Claude -> validate -> normalize,
emitting progress callbacks. Implemented in Sprint 4."""

from __future__ import annotations

from app.schemas.analysis import AnalysisResult, ManuscriptAnalysisRequest


class AnalysisOrchestrator:
    async def run(self, request: ManuscriptAnalysisRequest) -> AnalysisResult:
        raise NotImplementedError("Manuscript analysis is implemented in Sprint 4")
