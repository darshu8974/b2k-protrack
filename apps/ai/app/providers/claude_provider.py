"""Claude (Anthropic) provider adapter.

Structured output is produced via tool-use in Sprint 4. The Anthropic SDK is imported lazily
inside the method so the skeleton imports without the dependency installed.
"""

from __future__ import annotations

from typing import Any

from app.providers.base import LLMResponse


class ClaudeProvider:
    name = "claude"

    def __init__(self, api_key: str, model: str, max_tokens: int = 4096,
                 temperature: float = 0.2) -> None:
        self._api_key = api_key
        self._model = model
        self._max_tokens = max_tokens
        self._temperature = temperature

    async def generate_structured(
        self,
        *,
        system: str,
        user: str,
        output_schema: dict[str, Any],
        options: dict[str, Any] | None = None,
    ) -> LLMResponse:
        raise NotImplementedError("Claude structured generation is implemented in Sprint 4")
