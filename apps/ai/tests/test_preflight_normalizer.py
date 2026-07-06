"""PreflightNormalizer tests: deterministic outcomes + LLM phrasing -> PreflightResult."""

from __future__ import annotations

from app.preflight.models import CheckOutcome, Finding
from app.providers.base import LLMResponse, Usage
from app.schemas.common import CheckResult, Severity
from app.services.preflight_normalizer import PreflightNormalizer


def _fail_font_outcome() -> CheckOutcome:
    return CheckOutcome(
        key="font_embedding",
        result=CheckResult.FAIL,
        detail="1 font(s) not embedded: Arial.",
        finding=Finding(
            check_key="font_embedding",
            category="fonts",
            result=CheckResult.FAIL,
            page_ref=None,
            default_severity=Severity.HIGH,
            default_title="Fonts not embedded",
            default_recommendation="Embed all fonts.",
            evidence="1 font(s) not embedded: Arial.",
        ),
    )


def _pass_outcome(key: str) -> CheckOutcome:
    return CheckOutcome(key=key, result=CheckResult.PASS, detail="ok")


def _llm(issues: list[dict]) -> LLMResponse:
    return LLMResponse(data={"issues": issues}, usage=Usage(model="mock"))


def test_checks_and_score_are_deterministic_not_from_llm() -> None:
    outcomes = [_pass_outcome("geometry"), _fail_font_outcome()]
    # Even if the LLM lied about severity, checks/score/passed come from the deterministic outcomes.
    lie = [{"checkKey": "font_embedding", "severity": "LOW", "title": "x", "recommendation": "y"}]
    result = PreflightNormalizer().normalize(
        outcomes, _llm(lie), standard="PDF/X-4", prompt_id="preflight_findings.v1",
    )
    assert [c.key for c in result.checks] == ["geometry", "font_embedding"]
    assert result.checks[1].result is CheckResult.FAIL
    assert result.overall_score == 66 and result.passed is False  # 100 - 34 (one FAIL)
    assert result.standard == "PDF/X-4"
    assert result.prompt_id == "preflight_findings.v1"


def test_llm_phrasing_overlays_onto_finding() -> None:
    result = PreflightNormalizer().normalize(
        [_fail_font_outcome()],
        _llm([{
            "checkKey": "font_embedding",
            "severity": "HIGH",
            "title": "Embed the fonts",
            "recommendation": "Outline or embed every typeface.",
        }]),
    )
    assert len(result.issues) == 1
    issue = result.issues[0]
    assert issue.title == "Embed the fonts"
    assert issue.recommendation == "Outline or embed every typeface."
    assert issue.severity is Severity.HIGH
    # Category / source stay deterministic regardless of the LLM.
    assert issue.category == "fonts" and issue.source == "AI"
    assert result.totals.issues == 1 and result.totals.high == 1


def test_falls_back_to_defaults_when_llm_omits_or_is_invalid() -> None:
    # A bogus severity and blank text from the LLM -> deterministic defaults hold.
    bad = [
        {"checkKey": "font_embedding", "severity": "CRITICAL", "title": "", "recommendation": " "}
    ]
    result = PreflightNormalizer().normalize([_fail_font_outcome()], _llm(bad))
    issue = result.issues[0]
    assert issue.severity is Severity.HIGH  # default (bad enum ignored)
    assert issue.title == "Fonts not embedded"  # default (blank ignored)
    assert issue.recommendation == "Embed all fonts."


def test_no_findings_yields_no_issues() -> None:
    outcomes = [_pass_outcome("geometry"), _pass_outcome("overflow")]
    result = PreflightNormalizer().normalize(outcomes, _llm([]))
    assert result.issues == [] and result.overall_score == 100 and result.passed is True
    assert result.totals.issues == 0 and result.totals.high == 0


def test_serialized_result_is_camel_case() -> None:
    result = PreflightNormalizer().normalize([_fail_font_outcome()], _llm([]))
    dumped = result.model_dump(by_alias=True)
    assert "overallScore" in dumped
    assert dumped["issues"][0]["pageRef"] is None
    assert "promptId" in dumped
