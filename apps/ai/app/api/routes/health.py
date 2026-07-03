"""Public liveness/readiness endpoints for Render probes and Spring Boot health checks.
These are intentionally unauthenticated."""

from __future__ import annotations

from fastapi import APIRouter, Depends

from app.core.config import Settings, get_settings

router = APIRouter(prefix="/internal/v1", tags=["health"])


@router.get("/health")
async def health(settings: Settings = Depends(get_settings)) -> dict:
    return {"status": "UP", "service": settings.app_name, "version": settings.version}


@router.get("/ready")
async def ready(settings: Settings = Depends(get_settings)) -> dict:
    return {
        "status": "READY",
        "provider": settings.llm_provider,
        "model": settings.active_model,
    }
