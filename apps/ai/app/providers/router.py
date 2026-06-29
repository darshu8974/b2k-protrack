"""Provider selection — returns the active LLMProvider based on settings."""

from __future__ import annotations

from app.core.config import get_settings
from app.providers.base import LLMProvider
from app.providers.claude_provider import ClaudeProvider


def get_provider() -> LLMProvider:
    """Resolve the configured LLM provider (claude now; openai/gemini are future)."""
    settings = get_settings()
    if settings.llm_provider == "claude":
        return ClaudeProvider(
            api_key=settings.anthropic_api_key,
            model=settings.claude_model,
            max_tokens=settings.llm_max_tokens,
            temperature=settings.llm_temperature,
        )
    raise ValueError(f"Unsupported LLM provider: {settings.llm_provider}")
