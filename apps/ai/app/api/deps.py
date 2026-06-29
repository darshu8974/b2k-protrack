"""Shared FastAPI dependencies."""

from __future__ import annotations

from fastapi import Depends

from app.core.config import Settings, get_settings
from app.core.security import verify_internal_key
from app.providers.base import LLMProvider
from app.providers.router import get_provider


def settings_dep() -> Settings:
    return get_settings()


def provider_dep() -> LLMProvider:
    return get_provider()


# Reusable guard for all internal business endpoints.
InternalAuth = Depends(verify_internal_key)
