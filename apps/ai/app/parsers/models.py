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


# ── Production-PDF preflight facts (deterministic; consumed by the preflight checks) ──


class PageGeometry(BaseModel):
    index: int
    width_pt: float
    height_pt: float
    has_trimbox: bool = False
    has_bleedbox: bool = False
    rotation: int = 0
    content_overflow: bool = False  # content extends beyond the page box
    min_margin_pt: float | None = None  # smallest gap from content bbox to a page edge


class FontUsage(BaseModel):
    name: str
    embedded: bool


class ImageInfo(BaseModel):
    page_index: int
    dpi: float | None = None
    colorspace: str | None = None


class AccessibilityInfo(BaseModel):
    tagged: bool = False
    has_lang: bool = False
    has_title: bool = False


class PdfFacts(BaseModel):
    """Deterministic facts extracted from a production PDF for preflight (never judgement)."""

    page_count: int = 0
    pages: list[PageGeometry] = []
    fonts: list[FontUsage] = []
    images: list[ImageInfo] = []
    accessibility: AccessibilityInfo = AccessibilityInfo()
