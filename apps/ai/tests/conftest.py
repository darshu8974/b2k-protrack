"""Shared test fixtures — builds a real DOCX manuscript in memory."""

from __future__ import annotations

from io import BytesIO
from pathlib import Path

import pytest
from docx import Document


def _build_docx() -> bytes:
    document = Document()
    document.add_heading("Introduction", level=1)
    document.add_paragraph("This chapter introduces the subject in detail. " * 20)
    document.add_heading("Methods", level=2)
    document.add_paragraph("Problem 1. Solve for x given the constraints.")
    document.add_paragraph("Exercise 2. Prove the stated theorem.")
    document.add_table(rows=2, cols=3)
    document.add_heading("References", level=1)
    document.add_paragraph("[1] Author A, A Foundational Text, 2019.")
    document.add_paragraph("[2] Author B, Another Reference, 2021.")
    buffer = BytesIO()
    document.save(buffer)
    return buffer.getvalue()


@pytest.fixture
def sample_docx_bytes() -> bytes:
    return _build_docx()


@pytest.fixture
def sample_docx_url(tmp_path: Path) -> str:
    path = tmp_path / "manuscript.docx"
    path.write_bytes(_build_docx())
    return path.as_uri()  # file:///... URL, mirroring the local storage driver's signed URL
