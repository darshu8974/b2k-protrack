"""Shared FastAPI dependencies."""

from __future__ import annotations

from fastapi import Depends

from app.core.config import Settings, get_settings
from app.core.security import verify_internal_key
from app.orchestration.analysis_orchestrator import AnalysisOrchestrator
from app.orchestration.assistant_orchestrator import AssistantOrchestrator
from app.orchestration.preflight_orchestrator import PreflightOrchestrator
from app.orchestration.progress import ProgressReporter
from app.preflight.runner import PreflightRunner
from app.prompts.registry import PromptRegistry
from app.providers.base import LLMProvider
from app.providers.router import get_provider
from app.services.normalizer import AnalysisNormalizer
from app.services.preflight_normalizer import PreflightNormalizer
from app.storage.file_loader import FileLoader


def settings_dep() -> Settings:
    return get_settings()


def provider_dep() -> LLMProvider:
    return get_provider()


def analysis_orchestrator_dep(
    settings: Settings = Depends(get_settings),
    provider: LLMProvider = Depends(provider_dep),
) -> AnalysisOrchestrator:
    """Assemble the manuscript-analysis orchestrator from settings and the active provider."""
    return AnalysisOrchestrator(
        provider=provider,
        file_loader=FileLoader(settings.internal_key, settings.request_timeout_ms),
        prompt_registry=PromptRegistry(),
        normalizer=AnalysisNormalizer(),
        reporter=ProgressReporter(settings.spring_callback_base_url, settings.internal_key),
    )


def preflight_orchestrator_dep(
    settings: Settings = Depends(get_settings),
    provider: LLMProvider = Depends(provider_dep),
) -> PreflightOrchestrator:
    """Assemble the PDF-preflight orchestrator from settings and the active provider."""
    return PreflightOrchestrator(
        provider=provider,
        file_loader=FileLoader(settings.internal_key, settings.request_timeout_ms),
        prompt_registry=PromptRegistry(),
        runner=PreflightRunner(),
        normalizer=PreflightNormalizer(),
        reporter=ProgressReporter(settings.spring_callback_base_url, settings.internal_key),
    )


def assistant_orchestrator_dep(
    provider: LLMProvider = Depends(provider_dep),
) -> AssistantOrchestrator:
    """Assemble the scoped-assistant orchestrator from the active provider. Synchronous — no file
    loader or progress reporter (a chat turn fetches nothing and reports no job progress)."""
    return AssistantOrchestrator(provider=provider, prompt_registry=PromptRegistry())


# Reusable guard for all internal business endpoints.
InternalAuth = Depends(verify_internal_key)
