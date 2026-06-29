"""Orchestrates PDF preflight: load -> parse -> checks -> LLM phrasing -> normalize,
emitting progress callbacks. Implemented in Sprint 5."""

from __future__ import annotations

from app.schemas.preflight import PdfPreflightRequest, PreflightResult


class PreflightOrchestrator:
    async def run(self, request: PdfPreflightRequest) -> PreflightResult:
        raise NotImplementedError("PDF preflight is implemented in Sprint 5")
