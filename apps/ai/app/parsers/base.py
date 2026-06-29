"""Document parser abstraction. Parsers produce deterministic facts (counts, headings),
never judgement — that is the LLM's role."""

from __future__ import annotations

from typing import Protocol, runtime_checkable

from app.parsers.models import ParsedDocument


@runtime_checkable
class DocumentParser(Protocol):
    supported_types: tuple[str, ...]

    def parse(self, content: bytes, *, filename: str | None = None) -> ParsedDocument:
        """Parse raw bytes into a ParsedDocument."""
        ...
