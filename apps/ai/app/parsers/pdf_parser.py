"""PDF parser (pdfplumber + pypdf).

Deterministic facts only: page count (pypdf), images/tables/fonts and text (pdfplumber). Headings
are inferred from font-size tiers relative to the body size — a lightweight, deterministic heuristic
(PDF has no explicit heading structure). Judgement remains the LLM's job.
"""

from __future__ import annotations

import re
from collections import Counter
from io import BytesIO
from typing import Any

import pdfplumber
from pypdf import PdfReader

from app.parsers.models import (
    AccessibilityInfo,
    Counts,
    FontUsage,
    Heading,
    ImageInfo,
    PageGeometry,
    ParsedDocument,
    PdfFacts,
)

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


# ── Preflight fact extraction ────────────────────────────────────────────────
# Production-PDF preflight needs facts the analysis path does not (page geometry, font embedding,
# image DPI, content bounds). These are extracted deterministically here; the preflight checks apply
# thresholds and the LLM only phrases the findings. Kept separate from PdfParser.parse so the
# analysis pipeline is untouched.

_OVERFLOW_TOLERANCE_PT = 2.0  # ignore sub-2pt spill (rounding / hairline strokes)
_FONT_FILE_KEYS = ("/FontFile", "/FontFile2", "/FontFile3")
_SUBSET_PREFIX_RE = re.compile(r"^[A-Z]{6}\+")


def extract_pdf_facts(content: bytes) -> PdfFacts:
    """Extract deterministic preflight facts from a production PDF (pypdf + pdfplumber)."""
    reader = PdfReader(BytesIO(content))
    pages = _page_geometries(reader)
    fonts = _collect_fonts(reader)
    accessibility = _accessibility(reader)

    images, overflow_by_page, margin_by_page = _plumb_visuals(content)
    for geom in pages:
        if geom.index in overflow_by_page:
            geom.content_overflow = overflow_by_page[geom.index]
        if geom.index in margin_by_page:
            geom.min_margin_pt = margin_by_page[geom.index]

    return PdfFacts(
        page_count=len(pages),
        pages=pages,
        fonts=fonts,
        images=images,
        accessibility=accessibility,
    )


def _page_geometries(reader: Any) -> list[PageGeometry]:
    result: list[PageGeometry] = []
    for index, page in enumerate(reader.pages):
        box = page.mediabox
        try:
            rotation = int(page.get("/Rotate", 0) or 0)
        except (TypeError, ValueError):
            rotation = 0
        result.append(
            PageGeometry(
                index=index,
                width_pt=round(float(box.width), 2),
                height_pt=round(float(box.height), 2),
                has_trimbox=page.get("/TrimBox") is not None,
                has_bleedbox=page.get("/BleedBox") is not None,
                rotation=rotation,
            )
        )
    return result


def _collect_fonts(reader: Any) -> list[FontUsage]:
    # name -> embedded (a font counts as embedded if any occurrence embeds it).
    seen: dict[str, bool] = {}
    for page in reader.pages:
        resources = page.get("/Resources")
        if resources is None:
            continue
        fonts = resources.get_object().get("/Font")
        if fonts is None:
            continue
        for ref in fonts.get_object().values():
            font = ref.get_object()
            name = _clean_font_name(str(font.get("/BaseFont") or font.get("/Name") or "unknown"))
            seen[name] = seen.get(name, False) or _font_is_embedded(font)
    return [FontUsage(name=name, embedded=embedded) for name, embedded in sorted(seen.items())]


def _font_is_embedded(font: Any) -> bool:
    descriptor = font.get("/FontDescriptor")
    if descriptor is not None and _descriptor_has_file(descriptor.get_object()):
        return True
    # Composite (Type0) fonts carry the descriptor on their descendant fonts.
    descendants = font.get("/DescendantFonts")
    if descendants is not None:
        for sub in descendants.get_object():
            sub_desc = sub.get_object().get("/FontDescriptor")
            if sub_desc is not None and _descriptor_has_file(sub_desc.get_object()):
                return True
    return False


def _descriptor_has_file(descriptor: Any) -> bool:
    return any(key in descriptor for key in _FONT_FILE_KEYS)


def _clean_font_name(name: str) -> str:
    cleaned = name.lstrip("/")
    return _SUBSET_PREFIX_RE.sub("", cleaned)  # drop subset tags like "ABCDEF+"


def _accessibility(reader: Any) -> AccessibilityInfo:
    try:
        root = reader.trailer["/Root"].get_object()
    except Exception:  # noqa: BLE001 — malformed catalog: report as not-accessible, never crash
        return AccessibilityInfo()
    tagged = root.get("/StructTreeRoot") is not None
    mark_info = root.get("/MarkInfo")
    if not tagged and mark_info is not None:
        tagged = bool(mark_info.get_object().get("/Marked"))
    has_lang = bool(root.get("/Lang"))
    try:
        metadata = reader.metadata
        has_title = bool(metadata and metadata.title)
    except Exception:  # noqa: BLE001 — metadata is optional
        has_title = False
    return AccessibilityInfo(tagged=tagged, has_lang=has_lang, has_title=has_title)


def _plumb_visuals(
    content: bytes,
) -> tuple[list[ImageInfo], dict[int, bool], dict[int, float]]:
    images: list[ImageInfo] = []
    overflow: dict[int, bool] = {}
    margins: dict[int, float] = {}
    try:
        with pdfplumber.open(BytesIO(content)) as pdf:
            for index, page in enumerate(pdf.pages):
                try:
                    _plumb_page(index, page, images, overflow, margins)
                except Exception:  # noqa: BLE001 — per-page best-effort; skip a bad page
                    continue
    except Exception:  # noqa: BLE001 — pdfplumber may reject a PDF pypdf accepts; degrade gracefully
        return images, overflow, margins
    return images, overflow, margins


def _plumb_page(
    index: int,
    page: Any,
    images: list[ImageInfo],
    overflow: dict[int, bool],
    margins: dict[int, float],
) -> None:
    page_width = float(page.width)
    page_height = float(page.height)

    for img in page.images or []:
        colorspace = img.get("colorspace") or img.get("colorspacename")
        images.append(
            ImageInfo(
                page_index=index,
                dpi=_image_dpi(img),
                colorspace=str(colorspace) if colorspace else None,
            )
        )

    objects = (
        list(page.chars or [])
        + list(page.images or [])
        + list(page.rects or [])
        + list(page.lines or [])
    )
    boxes = [
        (float(o["x0"]), float(o["x1"]), float(o["top"]), float(o["bottom"]))
        for o in objects
        if _has_bbox(o)
    ]
    if not boxes:
        return
    left = min(b[0] for b in boxes)
    right = max(b[1] for b in boxes)
    top = min(b[2] for b in boxes)
    bottom = max(b[3] for b in boxes)
    overflow[index] = (
        left < -_OVERFLOW_TOLERANCE_PT
        or top < -_OVERFLOW_TOLERANCE_PT
        or right > page_width + _OVERFLOW_TOLERANCE_PT
        or bottom > page_height + _OVERFLOW_TOLERANCE_PT
    )
    margins[index] = round(
        min(left, top, page_width - right, page_height - bottom), 2
    )


def _has_bbox(obj: Any) -> bool:
    return all(key in obj for key in ("x0", "x1", "top", "bottom"))


def _image_dpi(img: Any) -> float | None:
    srcsize = img.get("srcsize")
    x0, x1, top, bottom = img.get("x0"), img.get("x1"), img.get("top"), img.get("bottom")
    if not srcsize or None in (x0, x1, top, bottom):
        return None
    display_w_in = (float(x1) - float(x0)) / 72.0
    display_h_in = (float(bottom) - float(top)) / 72.0
    if display_w_in <= 0 or display_h_in <= 0:
        return None
    return round(min(srcsize[0] / display_w_in, srcsize[1] / display_h_in), 1)
