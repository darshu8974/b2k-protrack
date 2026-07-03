"""Smoke tests for the health/readiness endpoints and route wiring."""

from __future__ import annotations

from fastapi.testclient import TestClient

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
    assert body["provider"] == "mock"
    assert body["model"] == "mock"


def test_business_route_requires_internal_key() -> None:
    response = client.post(
        "/internal/v1/analyze/manuscript",
        json={"jobId": "j1", "fileUrl": "file:///x", "docType": "docx"},
    )
    assert response.status_code == 401
