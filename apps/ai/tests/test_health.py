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


def test_ready_returns_provider() -> None:
    response = client.get("/internal/v1/ready")
    assert response.status_code == 200
    assert response.json()["provider"] == "claude"


def test_business_route_requires_internal_key() -> None:
    # Without X-Internal-Key the analyze route is rejected by the auth guard.
    response = client.post("/internal/v1/analyze/manuscript", json={
        "job_id": "j1", "file_url": "http://x", "doc_type": "docx",
    })
    assert response.status_code == 401
