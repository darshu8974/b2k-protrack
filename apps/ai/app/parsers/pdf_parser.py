"""PDF parser (pdfplumber + pypdf). Implemented in Sprint 4 (manuscript) / Sprint 5 (preflight).
Libraries are imported lazily."""

from __future__ import annotations

from app.parsers.models import ParsedDocument


class PdfParser:
    supported_types = ("application/pdf", "pdf")

    def parse(self, content: bytes, *, filename: str | None = None) -> ParsedDocument:
        raise NotImplementedError("PDF parsing is implemented in Sprint 4")
