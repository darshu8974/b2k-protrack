"""Uniform parsed-document model produced by all parsers (source-agnostic)."""

from __future__ import annotations

from pydantic import BaseModel


class Counts(BaseModel):
    pages: int = 0
    figures: int = 0
    tables: int = 0
    equations: int = 0
    problems: int = 0
    references: int = 0


class Heading(BaseModel):
    level: str  # H1 | H2 | H3
    text: str


class ParsedDocument(BaseModel):
    counts: Counts = Counts()
    headings: list[Heading] = []
    language: str | None = None
    fonts: list[str] = []
    raw_text_sample: str | None = None
