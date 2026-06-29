"""Runs the registered preflight checks against a parsed PDF. Implemented in Sprint 5."""

from __future__ import annotations

from app.parsers.models import ParsedDocument
from app.schemas.preflight import PreflightCheck, PreflightIssue


class PreflightRunner:
    def run(
        self, parsed: ParsedDocument, *, standard: str | None = None
    ) -> tuple[list[PreflightCheck], list[PreflightIssue]]:
        raise NotImplementedError("Preflight checks are implemented in Sprint 5")
