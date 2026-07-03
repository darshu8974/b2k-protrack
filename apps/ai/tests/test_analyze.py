"""End-to-end analyze-route test: real DOCX via file:// URL through the mock pipeline."""

from __future__ import annotations

from collections.abc import Iterator

import pytest
from fastapi.testclient import TestClient

from app.api.deps import analysis_orchestrator_dep
from app.main import app
from app.orchestration.analysis_orchestrator import AnalysisOrchestrator
from app.orchestration.progress import NoOpProgressReporter
from app.prompts.registry import PromptRegistry
from app.providers.mock_provider import MockProvider
from app.services.normalizer import AnalysisNormalizer
from app.storage.file_loader import FileLoader

_HEADERS = {"X-Internal-Key": "dev-internal-key"}


@pytest.fixture
def client() -> Iterator[TestClient]:
    # Override the orchestrator so progress callbacks are a no-op (hermetic, no Spring needed).
    def _orchestrator() -> AnalysisOrchestrator:
        return AnalysisOrchestrator(
            provider=MockProvider(),
            file_loader=FileLoader(internal_key="dev-internal-key"),
            prompt_registry=PromptRegistry(),
            normalizer=AnalysisNormalizer(),
            reporter=NoOpProgressReporter(),
        )

    app.dependency_overrides[analysis_orchestrator_dep] = _orchestrator
    yield TestClient(app)
    app.dependency_overrides.clear()


def test_analyze_manuscript_end_to_end(client: TestClient, sample_docx_url: str) -> None:
    response = client.post(
        "/internal/v1/analyze/manuscript",
        headers=_HEADERS,
        json={"jobId": "job-1", "fileUrl": sample_docx_url, "docType": "docx"},
    )
    assert response.status_code == 200, response.text
    body = response.json()

    # camelCase wire contract (matches the API spec + Spring's Jackson).
    assert 0 <= body["overallConfidence"] <= 100
    assert body["summary"]
    assert body["model"] == "mock"

    # Metrics come from the deterministic parser (full confidence); headings aggregated.
    metrics = {m["key"]: m for m in body["metrics"]}
    assert metrics["tables"]["value"] == 1
    assert metrics["references"]["value"] == 2
    assert metrics["pages"]["confidence"] == 100
    heading_levels = {h["level"] for h in body["headings"]}
    assert {"H1", "H2"}.issubset(heading_levels)

    # Judgement comes from the (mock) LLM.
    assert body["composition"]
    assert body["risks"][0]["severity"] in {"HIGH", "MEDIUM", "LOW"}
    assert body["suggestedTeam"][0]["matchScore"] <= 100


def test_analyze_is_deterministic(client: TestClient, sample_docx_url: str) -> None:
    payload = {"jobId": "job-2", "fileUrl": sample_docx_url, "docType": "docx"}
    first = client.post("/internal/v1/analyze/manuscript", headers=_HEADERS, json=payload)
    second = client.post("/internal/v1/analyze/manuscript", headers=_HEADERS, json=payload)
    assert first.json() == second.json()


def test_unsupported_doc_type_returns_structured_error(
    client: TestClient, sample_docx_url: str
) -> None:
    response = client.post(
        "/internal/v1/analyze/manuscript",
        headers=_HEADERS,
        json={"jobId": "job-3", "fileUrl": sample_docx_url, "docType": "epub"},
    )
    assert response.status_code == 400
    body = response.json()
    assert body["code"] == "PERMANENT"
    assert body["retryable"] is False
