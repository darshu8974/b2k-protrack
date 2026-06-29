"""Selects a parser by document type / mime. Phase 1 supports DOCX and PDF;
XML/LaTeX/EPUB/IDML register here in later phases with no orchestration change."""

from __future__ import annotations

from app.parsers.base import DocumentParser
from app.parsers.docx_parser import DocxParser
from app.parsers.pdf_parser import PdfParser


def get_parser(doc_type: str) -> DocumentParser:
    dt = doc_type.lower()
    if "docx" in dt or "word" in dt:
        return DocxParser()
    if "pdf" in dt:
        return PdfParser()
    raise ValueError(f"Unsupported document type: {doc_type}")
