"""Deterministic PDF fact-extraction tests (extract_pdf_facts)."""

from __future__ import annotations

import pytest

from app.core.errors import PermanentError
from app.parsers.models import PdfFacts
from app.parsers.pdf_parser import extract_pdf_facts


def test_extracts_page_geometry_from_blank_pdf(sample_pdf_bytes: bytes) -> None:
    facts = extract_pdf_facts(sample_pdf_bytes)

    assert isinstance(facts, PdfFacts)
    assert facts.page_count == 1
    assert len(facts.pages) == 1
    page = facts.pages[0]
    assert page.index == 0
    assert round(page.width_pt) == 612
    assert round(page.height_pt) == 792


def test_blank_pdf_has_no_fonts_or_images(sample_pdf_bytes: bytes) -> None:
    facts = extract_pdf_facts(sample_pdf_bytes)

    # A blank page declares no text fonts and embeds no raster images.
    assert facts.fonts == []
    assert facts.images == []


def test_blank_pdf_is_untagged(sample_pdf_bytes: bytes) -> None:
    facts = extract_pdf_facts(sample_pdf_bytes)

    # No structure tree / language -> accessibility check will REVIEW.
    assert facts.accessibility.tagged is False
    assert facts.accessibility.has_lang is False


def test_extraction_is_deterministic(sample_pdf_bytes: bytes) -> None:
    first = extract_pdf_facts(sample_pdf_bytes)
    second = extract_pdf_facts(sample_pdf_bytes)
    assert first.model_dump() == second.model_dump()


def test_permission_only_encrypted_pdf_extracts_like_a_plain_pdf(
    permission_only_pdf_bytes: bytes,
) -> None:
    facts = extract_pdf_facts(permission_only_pdf_bytes)
    assert facts.page_count == 1


def test_password_protected_pdf_raises_a_clean_permanent_error(
    password_protected_pdf_bytes: bytes,
) -> None:
    with pytest.raises(PermanentError, match="password-protected"):
        extract_pdf_facts(password_protected_pdf_bytes)
