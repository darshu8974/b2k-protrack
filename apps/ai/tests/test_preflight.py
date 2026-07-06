"""End-to-end preflight-route test: real PDF via file:// URL through the mock pipeline."""

from __future__ import annotations

from collections.abc import Iterator

import pytest
from fastapi.testclient import TestClient

from app.api.deps import preflight_orchestrator_dep
from app.main import app
from app.orchestration.preflight_orchestrator import PreflightOrchestrator
from app.orchestration.progress import NoOpProgressReporter
from app.preflight.runner import PreflightRunner
from app.prompts.registry import PromptRegistry
from app.providers.mock_provider import MockProvider
from app.services.preflight_normalizer import PreflightNormalizer
from app.storage.file_loader import FileLoader

_HEADERS = {"X-Internal-Key": "dev-internal-key"}
_CHECK_KEYS = {
    "geometry", "font_embedding", "image_resolution", "overflow", "placement", "accessibility",
}


@pytest.fixture
def client() -> Iterator[TestClient]:
    def _orchestrator() -> PreflightOrchestrator:
        return PreflightOrchestrator(
            provider=MockProvider(),
            file_loader=FileLoader(internal_key="dev-internal-key"),
            prompt_registry=PromptRegistry(),
            runner=PreflightRunner(),
            normalizer=PreflightNormalizer(),
            reporter=NoOpProgressReporter(),
        )

    app.dependency_overrides[preflight_orchestrator_dep] = _orchestrator
    yield TestClient(app)
    app.dependency_overrides.clear()


def test_preflight_pdf_end_to_end(client: TestClient, sample_pdf_url: str) -> None:
    response = client.post(
        "/internal/v1/preflight/pdf",
        headers=_HEADERS,
        json={"jobId": "job-1", "fileUrl": sample_pdf_url, "standard": "PDF/X-4"},
    )
    assert response.status_code == 200, response.text
    body = response.json()

    # camelCase wire contract (matches the API spec + Spring's Jackson).
    assert 0 <= body["overallScore"] <= 100
    assert isinstance(body["passed"], bool)
    assert body["standard"] == "PDF/X-4"
    assert body["model"] == "mock"
    assert body["promptId"] == "preflight_findings.v1"

    # All six deterministic checks are reported, in order, with valid results.
    checks = {c["key"]: c["result"] for c in body["checks"]}
    assert [c["key"] for c in body["checks"]] == [
        "geometry", "font_embedding", "image_resolution", "overflow", "placement", "accessibility",
    ]
    assert set(checks) == _CHECK_KEYS
    assert all(result in {"PASS", "REVIEW", "FAIL"} for result in checks.values())

    # A blank PDF under PDF/X-4: no trim/bleed -> geometry REVIEW; untagged -> accessibility REVIEW.
    # The other four checks have nothing to flag. Two findings -> two mock-phrased issues.
    assert checks["geometry"] == "REVIEW"
    assert checks["accessibility"] == "REVIEW"
    assert checks["font_embedding"] == "PASS"
    assert body["overallScore"] == 80  # 100 - 2*10 (two REVIEWs)
    assert body["passed"] is True

    assert body["totals"]["issues"] == len(body["issues"]) == 2
    assert {issue["category"] for issue in body["issues"]} == {"layout", "accessibility"}
    assert all(issue["severity"] in {"HIGH", "MEDIUM", "LOW"} for issue in body["issues"])
    assert all(issue["source"] == "AI" for issue in body["issues"])


def test_preflight_is_deterministic(client: TestClient, sample_pdf_url: str) -> None:
    payload = {"jobId": "job-2", "fileUrl": sample_pdf_url, "standard": "PDF/X-4"}
    first = client.post("/internal/v1/preflight/pdf", headers=_HEADERS, json=payload)
    second = client.post("/internal/v1/preflight/pdf", headers=_HEADERS, json=payload)
    assert first.json() == second.json()


def test_preflight_requires_internal_key(client: TestClient, sample_pdf_url: str) -> None:
    response = client.post(
        "/internal/v1/preflight/pdf",
        json={"jobId": "job-3", "fileUrl": sample_pdf_url},
    )
    assert response.status_code == 401
