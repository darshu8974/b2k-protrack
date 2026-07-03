"""Provider selection — returns the active LLMProvider based on settings.

The provider is chosen purely by config (``AI_PROVIDER``); orchestration and prompts are
unchanged when it changes. ``mock`` is the deterministic default; ``claude`` is the real API.
OpenAI/Gemini adapters register here later with no orchestration change.
"""

from __future__ import annotations

from app.core.config import get_settings
from app.core.errors import PermanentError
from app.providers.base import LLMProvider
from app.providers.claude_provider import ClaudeProvider
from app.providers.mock_provider import MockProvider


def get_provider() -> LLMProvider:
    """Resolve the configured LLM provider."""
    settings = get_settings()
    provider = settings.llm_provider.lower()
    if provider == "mock":
        return MockProvider()
    if provider == "claude":
        return ClaudeProvider(
            api_key=settings.anthropic_api_key,
            model=settings.claude_model,
            max_tokens=settings.llm_max_tokens,
            temperature=settings.llm_temperature,
        )
    raise PermanentError(f"Unsupported LLM provider: {settings.llm_provider}")
