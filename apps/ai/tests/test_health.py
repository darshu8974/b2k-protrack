"""Smoke tests for the health/readiness endpoints and route wiring."""

from __future__ import annotations

from fastapi.testclient import TestClient

import app.api.routes.health as health_route
from app.core.errors import PermanentError
from app.main import app

client = TestClient(app)


def test_health_returns_up() -> None:
    response = client.get("/internal/v1/health")
    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "UP"
    assert body["service"] == "protrack-ai"


def test_ready_reports_active_provider() -> None:
    response = client.get("/internal/v1/ready")
    assert response.status_code == 200
    body = response.json()
    # Default provider is the deterministic mock (no API key required).
    assert body["status"] == "READY"
    assert body["provider"] == "mock"
    assert body["model"] == "mock"
    assert body["checks"]["provider"] is True


def test_ready_reports_not_ready_when_the_provider_is_misconfigured(monkeypatch) -> None:
    def _fail() -> None:
        raise PermanentError("ANTHROPIC_API_KEY is required for the Claude provider")

    # Simulate a misconfigured provider (e.g. Claude selected without an API key).
    monkeypatch.setattr(health_route, "get_provider", _fail)

    response = client.get("/internal/v1/ready")
    assert response.status_code == 503
    body = response.json()
    assert body["status"] == "NOT_READY"
    assert body["checks"]["provider"] is False
    assert body["detail"] == "PermanentError"


def test_business_route_requires_internal_key() -> None:
    response = client.post(
        "/internal/v1/analyze/manuscript",
        json={"jobId": "j1", "fileUrl": "file:///x", "docType": "docx"},
    )
    assert response.status_code == 401
