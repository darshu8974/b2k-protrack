"""Runs the registered preflight checks against extracted PDF facts.

Purely deterministic: each check inspects the parsed facts and returns its PASS/REVIEW/FAIL outcome.
The runner owns no LLM logic — phrasing of the resulting findings happens later in the orchestrator.
"""

from __future__ import annotations

from app.parsers.models import PdfFacts
from app.preflight.models import CheckOutcome
from app.preflight.registry import CHECKS


class PreflightRunner:
    def run(self, facts: PdfFacts, standard: str | None = None) -> list[CheckOutcome]:
        return [check(facts, standard) for _, check in CHECKS]
