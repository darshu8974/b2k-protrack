"""Internal preflight domain: the deterministic outcome of a single check.

A check returns a ``CheckOutcome`` (its PASS/REVIEW/FAIL result — the source of truth — plus a
factual detail). Non-PASS checks also carry a ``Finding``: the deterministic facts (category,
affected pages, evidence) plus default phrasing used when the LLM does not (or cannot) phrase it.
The LLM only refines a finding's severity/title/recommendation; it never adds or removes findings.
"""

from __future__ import annotations

from dataclasses import dataclass

from app.schemas.common import CheckResult, Severity


@dataclass
class Finding:
    check_key: str
    category: str
    result: CheckResult  # REVIEW or FAIL (a PASS produces no finding)
    page_ref: str | None
    default_severity: Severity
    default_title: str
    default_recommendation: str
    evidence: str  # factual summary shown to the LLM for phrasing


@dataclass
class CheckOutcome:
    key: str
    result: CheckResult
    detail: str
    finding: Finding | None = None


def page_ref(indices: list[int]) -> str | None:
    """Render 0-based page indices as a human 1-based page reference (e.g. 'pages 2, 5, 7')."""
    if not indices:
        return None
    ordered = sorted({index + 1 for index in indices})
    if len(ordered) == 1:
        return f"page {ordered[0]}"
    shown = ", ".join(str(number) for number in ordered[:10])
    if len(ordered) > 10:
        shown += ", …"
    return f"pages {shown}"
