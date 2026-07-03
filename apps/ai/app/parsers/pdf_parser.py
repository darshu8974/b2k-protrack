"""PDF parser (pdfplumber + pypdf).

Deterministic facts only: page count (pypdf), images/tables/fonts and text (pdfplumber). Headings
are inferred from font-size tiers relative to the body size — a lightweight, deterministic heuristic
(PDF has no explicit heading structure). Judgement remains the LLM's job.
"""

from __future__ import annotations

import re
from collections import Counter
from io import BytesIO

import pdfplumber
from pypdf import PdfReader

from app.parsers.models import Counts, Heading, ParsedDocument

_PROBLEM_RE = re.compile(r"(?im)^\s*(problem|exercise)\b")
_REFERENCE_ENTRY_RE = re.compile(r"^\s*(\[\d+\]|\d+\.)\s+\S")
_EQUATION_RE = re.compile(r"[=≤≥±∑∫√≈≠]")
_REFERENCE_HEADINGS = ("references", "bibliography", "works cited")
_HEADING_MAX_LEN = 90
_MAX_HEADINGS = 50
_SAMPLE_CHARS = 2000


class PdfParser:
    supported_types: tuple[str, ...] = ("application/pdf", "pdf")

    def parse(self, content: bytes, *, filename: str | None = None) -> ParsedDocument:
        pages = len(PdfReader(BytesIO(content)).pages)

        text_parts: list[str] = []
        lines: list[tuple[float, str]] = []
        fonts: set[str] = set()
        figures = 0
        tables = 0

        with pdfplumber.open(BytesIO(content)) as pdf:
            for page in pdf.pages:
                text_parts.append(page.extract_text() or "")
                figures += len(page.images or [])
                tables += len(page.find_tables() or [])
                for char in page.chars:
                    name = char.get("fontname")
                    if name:
                        fonts.add(str(name))
                lines.extend(_page_lines(page))

        full_text = "\n".join(part for part in text_parts if part)
        counts = Counts(
            pages=pages,
            figures=figures,
            tables=tables,
            equations=_count_equations(full_text),
            problems=len(_PROBLEM_RE.findall(full_text)),
            references=_count_references(full_text),
        )
        return ParsedDocument(
            counts=counts,
            headings=_infer_headings(lines),
            language=None,
            fonts=sorted(fonts)[:10],
            raw_text_sample=full_text[:_SAMPLE_CHARS] or None,
        )


def _page_lines(page) -> list[tuple[float, str]]:  # type: ignore[no-untyped-def]
    """Group a page's chars into lines of (max_font_size, text)."""
    grouped: dict[int, list[dict]] = {}
    for char in page.chars:
        grouped.setdefault(round(char["top"]), []).append(char)
    result: list[tuple[float, str]] = []
    for key in sorted(grouped):
        chars = grouped[key]
        text = "".join(c["text"] for c in chars).strip()
        if not text:
            continue
        size = round(max(float(c.get("size", 0.0)) for c in chars), 1)
        result.append((size, text))
    return result


def _infer_headings(lines: list[tuple[float, str]]) -> list[Heading]:
    if not lines:
        return []
    body_size = Counter(size for size, _ in lines).most_common(1)[0][0]
    larger = sorted({size for size, _ in lines if size > body_size}, reverse=True)[:3]
    if not larger:
        return []
    level_by_size = {size: f"H{index + 1}" for index, size in enumerate(larger)}
    headings: list[Heading] = []
    for size, text in lines:
        level = level_by_size.get(size)
        if level and len(text) <= _HEADING_MAX_LEN:
            headings.append(Heading(level=level, text=text[:200]))
            if len(headings) >= _MAX_HEADINGS:
                break
    return headings


def _count_equations(text: str) -> int:
    return sum(
        1
        for line in text.splitlines()
        if len(line) < 60 and _EQUATION_RE.search(line)
    )


def _count_references(text: str) -> int:
    lines = text.splitlines()
    start = None
    for index, line in enumerate(lines):
        if line.strip().lower() in _REFERENCE_HEADINGS:
            start = index + 1
    if start is None:
        return 0
    return sum(1 for line in lines[start:] if _REFERENCE_ENTRY_RE.match(line))
