"""Public liveness/readiness endpoints for Render probes and Spring Boot health checks.
These are intentionally unauthenticated.

``/health`` is pure liveness (the process is up). ``/ready`` additionally validates that the
configured LLM provider is usable — resolving it exercises its construction-time checks (e.g. the
Claude/Gemini providers each require their own API key), so a misconfigured provider reports
NOT_READY (503) rather than failing on the first real request.
"""

from __future__ import annotations

from fastapi import APIRouter, Depends, Response, status

from app.core.config import Settings, get_settings
from app.core.logging import get_logger
from app.providers.router import get_provider

router = APIRouter(prefix="/internal/v1", tags=["health"])
logger = get_logger(__name__)


@router.get("/health")
async def health(settings: Settings = Depends(get_settings)) -> dict:
    return {"status": "UP", "service": settings.app_name, "version": settings.version}


@router.get("/ready")
async def ready(response: Response, settings: Settings = Depends(get_settings)) -> dict:
    """Readiness = the configured provider resolves without a configuration error."""
    provider_ready = True
    detail: str | None = None
    try:
        get_provider()  # unwrapped factory: raises on a misconfigured provider (e.g. missing key)
    except Exception as exc:
        provider_ready = False
        detail = type(exc).__name__
        logger.warning("provider_not_ready", provider=settings.llm_provider, error=detail)

    if not provider_ready:
        response.status_code = status.HTTP_503_SERVICE_UNAVAILABLE

    body: dict = {
        "status": "READY" if provider_ready else "NOT_READY",
        "provider": settings.llm_provider,
        "model": settings.active_model,
        "checks": {"provider": provider_ready},
    }
    if detail is not None:
        body["detail"] = detail
    return body
