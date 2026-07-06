"""Deterministic preflight-check tests: each check against synthetic PdfFacts, plus scoring."""

from __future__ import annotations

from app.parsers.models import (
    AccessibilityInfo,
    FontUsage,
    ImageInfo,
    PageGeometry,
    PdfFacts,
)
from app.preflight.checks import (
    accessibility,
    font_embedding,
    geometry,
    image_resolution,
    overflow,
    placement,
)
from app.preflight.runner import PreflightRunner
from app.preflight.scoring import PASS_THRESHOLD, score_and_pass
from app.schemas.common import CheckResult, Severity


def _facts(**kwargs: object) -> PdfFacts:
    base: dict[str, object] = {
        "page_count": 1,
        "pages": [PageGeometry(index=0, width_pt=612, height_pt=792, min_margin_pt=72.0)],
        "fonts": [],
        "images": [],
        "accessibility": AccessibilityInfo(tagged=True, has_lang=True, has_title=True),
    }
    base.update(kwargs)
    return PdfFacts(**base)  # type: ignore[arg-type]


# ── geometry ──
def test_geometry_pass_uniform() -> None:
    outcome = geometry.check(_facts(), None)
    assert outcome.result is CheckResult.PASS and outcome.finding is None


def test_geometry_review_mixed_sizes() -> None:
    pages = [
        PageGeometry(index=0, width_pt=612, height_pt=792),
        PageGeometry(index=1, width_pt=595, height_pt=842),
    ]
    outcome = geometry.check(_facts(pages=pages, page_count=2), None)
    assert outcome.result is CheckResult.REVIEW
    assert outcome.finding is not None and outcome.finding.default_severity is Severity.MEDIUM


def test_geometry_review_missing_marks_for_print_standard() -> None:
    outcome = geometry.check(_facts(), "PDF/X-4")
    assert outcome.result is CheckResult.REVIEW
    assert outcome.finding is not None and outcome.finding.category == "layout"


def test_geometry_pass_with_trim_for_print_standard() -> None:
    pages = [PageGeometry(index=0, width_pt=612, height_pt=792, has_trimbox=True)]
    outcome = geometry.check(_facts(pages=pages), "PDF/X-4")
    assert outcome.result is CheckResult.PASS


# ── font embedding ──
def test_fonts_pass_all_embedded() -> None:
    fonts = [FontUsage(name="Minion", embedded=True), FontUsage(name="Helvetica", embedded=True)]
    assert font_embedding.check(_facts(fonts=fonts), None).result is CheckResult.PASS


def test_fonts_pass_when_none_declared() -> None:
    assert font_embedding.check(_facts(fonts=[]), None).result is CheckResult.PASS


def test_fonts_fail_when_not_embedded() -> None:
    fonts = [FontUsage(name="Minion", embedded=True), FontUsage(name="Arial", embedded=False)]
    outcome = font_embedding.check(_facts(fonts=fonts), None)
    assert outcome.result is CheckResult.FAIL
    assert outcome.finding is not None
    assert outcome.finding.default_severity is Severity.HIGH
    assert "Arial" in outcome.detail


# ── image resolution ──
def test_images_pass_high_dpi() -> None:
    images = [ImageInfo(page_index=0, dpi=350.0)]
    assert image_resolution.check(_facts(images=images), None).result is CheckResult.PASS


def test_images_pass_when_none() -> None:
    assert image_resolution.check(_facts(images=[]), None).result is CheckResult.PASS


def test_images_review_borderline() -> None:
    images = [ImageInfo(page_index=1, dpi=220.0)]
    outcome = image_resolution.check(_facts(images=images), None)
    assert outcome.result is CheckResult.REVIEW
    assert outcome.finding is not None and outcome.finding.page_ref == "page 2"


def test_images_fail_low_dpi() -> None:
    images = [ImageInfo(page_index=0, dpi=90.0), ImageInfo(page_index=2, dpi=400.0)]
    outcome = image_resolution.check(_facts(images=images), None)
    assert outcome.result is CheckResult.FAIL
    assert outcome.finding is not None and outcome.finding.default_severity is Severity.HIGH


def test_images_pass_when_dpi_unmeasurable() -> None:
    images = [ImageInfo(page_index=0, dpi=None)]
    assert image_resolution.check(_facts(images=images), None).result is CheckResult.PASS


# ── overflow ──
def test_overflow_pass() -> None:
    assert overflow.check(_facts(), None).result is CheckResult.PASS


def test_overflow_fail() -> None:
    pages = [PageGeometry(index=0, width_pt=612, height_pt=792, content_overflow=True)]
    outcome = overflow.check(_facts(pages=pages), None)
    assert outcome.result is CheckResult.FAIL
    assert outcome.finding is not None and outcome.finding.page_ref == "page 1"


# ── placement ──
def test_placement_pass_wide_margin() -> None:
    assert placement.check(_facts(), None).result is CheckResult.PASS


def test_placement_review_tight_margin() -> None:
    pages = [PageGeometry(index=0, width_pt=612, height_pt=792, min_margin_pt=5.0)]
    outcome = placement.check(_facts(pages=pages), None)
    assert outcome.result is CheckResult.REVIEW
    assert outcome.finding is not None and outcome.finding.default_severity is Severity.MEDIUM


def test_placement_ignores_overflowing_pages() -> None:
    # Overflow is reported by its own check; placement should not double-count it.
    pages = [
        PageGeometry(
            index=0, width_pt=612, height_pt=792, content_overflow=True, min_margin_pt=-5.0
        )
    ]
    assert placement.check(_facts(pages=pages), None).result is CheckResult.PASS


# ── accessibility ──
def test_accessibility_pass_tagged_with_lang() -> None:
    assert accessibility.check(_facts(), None).result is CheckResult.PASS


def test_accessibility_review_untagged() -> None:
    facts = _facts(accessibility=AccessibilityInfo(tagged=False))
    outcome = accessibility.check(facts, None)
    assert outcome.result is CheckResult.REVIEW
    assert outcome.finding is not None and outcome.finding.default_severity is Severity.LOW


def test_accessibility_review_missing_language() -> None:
    facts = _facts(accessibility=AccessibilityInfo(tagged=True, has_lang=False))
    assert accessibility.check(facts, None).result is CheckResult.REVIEW


# ── runner + scoring ──
def test_runner_returns_one_outcome_per_check() -> None:
    outcomes = PreflightRunner().run(_facts(), None)
    assert [o.key for o in outcomes] == [
        "geometry",
        "font_embedding",
        "image_resolution",
        "overflow",
        "placement",
        "accessibility",
    ]


def test_scoring_all_pass_is_100() -> None:
    outcomes = PreflightRunner().run(_facts(), None)
    score, passed = score_and_pass(outcomes)
    assert score == 100 and passed is True


def test_scoring_fail_pushes_below_threshold() -> None:
    fonts = [FontUsage(name="Arial", embedded=False)]
    outcomes = PreflightRunner().run(_facts(fonts=fonts), None)
    score, passed = score_and_pass(outcomes)
    assert score < PASS_THRESHOLD and passed is False
