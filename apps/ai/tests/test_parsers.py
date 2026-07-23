"""Deterministic parser tests (DOCX + PDF)."""

from __future__ import annotations

import pytest

from app.core.errors import PermanentError
from app.parsers.docx_parser import DocxParser
from app.parsers.factory import get_parser


def test_factory_selects_parsers() -> None:
    from app.parsers.pdf_parser import PdfParser

    assert isinstance(get_parser("docx"), DocxParser)
    assert isinstance(
        get_parser("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        DocxParser,
    )
    assert isinstance(get_parser("application/pdf"), PdfParser)


def test_docx_parser_extracts_deterministic_facts(sample_docx_bytes: bytes) -> None:
    parsed = DocxParser().parse(sample_docx_bytes)

    # Headings by level (Introduction=H1, Methods=H2, References=H1).
    levels = [h.level for h in parsed.headings]
    assert levels.count("H1") == 2
    assert levels.count("H2") == 1

    assert parsed.counts.tables == 1
    assert parsed.counts.problems == 2  # "Problem 1" + "Exercise 2"
    assert parsed.counts.references == 2  # two entries under the References heading
    assert parsed.counts.pages >= 1
    assert parsed.raw_text_sample is not None


def test_docx_parsing_is_deterministic(sample_docx_bytes: bytes) -> None:
    first = DocxParser().parse(sample_docx_bytes)
    second = DocxParser().parse(sample_docx_bytes)
    assert first.model_dump() == second.model_dump()


class TestPdfParserEncryption:
    """Regression coverage for a bug found during manual end-to-end testing: PdfParser.parse()
    crashed with an unhandled pypdf.errors.FileNotDecryptedError (-> raw 500) instead of either
    transparently opening a permission-only-encrypted PDF or reporting a clean, actionable error
    for a genuinely password-protected one."""

    def test_permission_only_encrypted_pdf_parses_like_a_plain_pdf(
        self, permission_only_pdf_bytes: bytes
    ) -> None:
        from app.parsers.pdf_parser import PdfParser

        parsed = PdfParser().parse(permission_only_pdf_bytes)
        assert parsed.counts.pages == 1

    def test_password_protected_pdf_raises_a_clean_permanent_error(
        self, password_protected_pdf_bytes: bytes
    ) -> None:
        from app.parsers.pdf_parser import PdfParser

        with pytest.raises(PermanentError, match="password-protected"):
            PdfParser().parse(password_protected_pdf_bytes)
