"""Claude (Anthropic) provider adapter.

Structured output via **tool-use**: a single forced tool whose ``input_schema`` is the response
JSON schema, so Claude returns validated structured data (not free text to parse). The Anthropic
SDK and its error types are imported lazily so the module imports even when the key/SDK are unused
(the default provider is the mock). The API key lives only in the AI service env.
"""

from __future__ import annotations

from typing import Any

from app.core.errors import OutputValidationError, PermanentError, TransientProviderError
from app.providers.base import LLMResponse, Usage
from app.providers.retry import llm_retry

_TOOL_NAME = "emit_manuscript_analysis"


class ClaudeProvider:
    name = "claude"

    def __init__(
        self,
        api_key: str,
        model: str,
        max_tokens: int = 4096,
        temperature: float = 0.2,
    ) -> None:
        if not api_key:
            raise PermanentError(
                "ANTHROPIC_API_KEY is required for the Claude provider "
                "(set AI_PROVIDER=mock for local development)."
            )
        self._api_key = api_key
        self._model = model
        self._max_tokens = max_tokens
        self._temperature = temperature
        self._client: Any = None

    def _get_client(self) -> Any:
        if self._client is None:
            from anthropic import AsyncAnthropic

            self._client = AsyncAnthropic(api_key=self._api_key)
        return self._client

    @llm_retry
    async def generate_structured(
        self,
        *,
        system: str,
        user: str,
        output_schema: dict[str, Any],
        options: dict[str, Any] | None = None,
    ) -> LLMResponse:
        from anthropic import (
            APIConnectionError,
            APIStatusError,
            APITimeoutError,
            RateLimitError,
        )

        client = self._get_client()
        tool = {
            "name": _TOOL_NAME,
            "description": "Return the structured manuscript analysis matching the schema.",
            "input_schema": output_schema,
        }
        try:
            response = await client.messages.create(
                model=self._model,
                max_tokens=self._max_tokens,
                temperature=self._temperature,
                system=system,
                messages=[{"role": "user", "content": user}],
                tools=[tool],
                tool_choice={"type": "tool", "name": _TOOL_NAME},
            )
        except (RateLimitError, APITimeoutError, APIConnectionError) as exc:
            raise TransientProviderError(f"Claude call failed transiently: {exc}") from exc
        except APIStatusError as exc:
            if exc.status_code >= 500:
                raise TransientProviderError(f"Claude server error: {exc}") from exc
            raise PermanentError(f"Claude rejected the request: {exc}") from exc

        data = _extract_tool_input(response)
        usage = Usage(
            input_tokens=getattr(response.usage, "input_tokens", 0),
            output_tokens=getattr(response.usage, "output_tokens", 0),
            model=getattr(response, "model", self._model),
        )
        return LLMResponse(data=data, usage=usage, raw=response)


def _extract_tool_input(response: Any) -> dict[str, Any]:
    """Pull the forced tool's structured input out of the response content blocks."""
    for block in getattr(response, "content", []) or []:
        is_tool = getattr(block, "type", None) == "tool_use"
        if is_tool and getattr(block, "name", None) == _TOOL_NAME:
            data = block.input
            if isinstance(data, dict):
                return data
    raise OutputValidationError("Claude did not return the expected structured tool output.")
