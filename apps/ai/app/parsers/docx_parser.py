"""DOCX parser (python-docx). Implemented in Sprint 4. The library is imported lazily."""

from __future__ import annotations

from app.parsers.models import ParsedDocument


class DocxParser:
    supported_types = (
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "docx",
    )

    def parse(self, content: bytes, *, filename: str | None = None) -> ParsedDocument:
        raise NotImplementedError("DOCX parsing is implemented in Sprint 4")
