"""Google Gemini provider adapter.

Structured output via Gemini's native JSON mode: ``response_mime_type="application/json"`` plus
``response_schema`` constrains the model to emit schema-valid JSON directly — the same "structured
output, not free-text parsing" principle as Claude's tool-use adapter. The ``options`` dict Claude
uses for its forced-tool identity (``tool_name``/``tool_description``) has no equivalent in
Gemini's JSON mode and is accepted-but-ignored, so the Protocol-neutral call from
``AssistantOrchestrator`` (which always passes ``options``) works unchanged. The google-genai SDK
is imported lazily so the module imports even when the key/SDK are unused (the default provider is
the mock). The API key lives only in the AI service env.
"""

from __future__ import annotations

import json
from typing import Any

import httpx

from app.core.errors import OutputValidationError, PermanentError, TransientProviderError
from app.providers.base import LLMResponse, Usage
from app.providers.retry import llm_retry


class GeminiProvider:
    name = "gemini"

    def __init__(
        self,
        api_key: str,
        model: str,
        max_tokens: int = 4096,
        temperature: float = 0.2,
    ) -> None:
        if not api_key:
            raise PermanentError(
                "GEMINI_API_KEY is required for the Gemini provider "
                "(set AI_PROVIDER=mock for local development)."
            )
        self._api_key = api_key
        self._model = model
        self._max_tokens = max_tokens
        self._temperature = temperature
        self._client: Any = None

    def _get_client(self) -> Any:
        if self._client is None:
            from google import genai

            self._client = genai.Client(api_key=self._api_key)
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
        from google.genai import errors, types

        client = self._get_client()
        config = types.GenerateContentConfig(
            system_instruction=system,
            temperature=self._temperature,
            max_output_tokens=self._max_tokens,
            response_mime_type="application/json",
            response_schema=_to_gemini_schema(output_schema),
        )
        try:
            response = await client.aio.models.generate_content(
                model=self._model, contents=user, config=config
            )
        except errors.ServerError as exc:
            raise TransientProviderError(f"Gemini server error: {exc}") from exc
        except errors.ClientError as exc:
            if exc.code == 429:
                raise TransientProviderError(f"Gemini rate limited: {exc}") from exc
            raise PermanentError(f"Gemini rejected the request: {exc}") from exc
        except httpx.TransportError as exc:
            raise TransientProviderError(f"Gemini call failed transiently: {exc}") from exc

        data = _extract_json(response)
        usage_metadata = response.usage_metadata
        usage = Usage(
            input_tokens=getattr(usage_metadata, "prompt_token_count", 0) or 0,
            output_tokens=getattr(usage_metadata, "candidates_token_count", 0) or 0,
            model=getattr(response, "model_version", None) or self._model,
        )
        return LLMResponse(data=data, usage=usage, raw=response)


def _extract_json(response: Any) -> dict[str, Any]:
    """Parse the schema-constrained JSON text Gemini returns into a dict."""
    text = getattr(response, "text", None)
    if not text:
        raise OutputValidationError("Gemini did not return any structured output text.")
    try:
        data = json.loads(text)
    except json.JSONDecodeError as exc:
        raise OutputValidationError(f"Gemini output was not valid JSON: {exc}") from exc
    if not isinstance(data, dict):
        raise OutputValidationError("Gemini output was valid JSON but not an object.")
    return data


def _to_gemini_schema(schema: dict[str, Any]) -> dict[str, Any]:
    """Convert a shared JSON-Schema output schema to Gemini's OpenAPI-subset dialect.

    Gemini's ``response_schema`` doesn't support JSON Schema's nullable-union form
    (``"type": ["string", "null"]``) the way Claude's tool-use does. This recursively rewrites any
    two-element ``[<type>, "null"]`` union into ``"type": <type>, "nullable": true`` — the
    OpenAPI-style shape Gemini expects. The shared schema file itself (single source of truth for
    Claude, the mock, and the Pydantic response models) is never modified; this conversion is
    local to the Gemini adapter only.
    """
    if not isinstance(schema, dict):
        return schema

    converted: dict[str, Any] = {}
    for key, value in schema.items():
        if key == "type" and isinstance(value, list):
            non_null = [t for t in value if t != "null"]
            if "null" in value and len(non_null) == 1:
                converted["type"] = non_null[0]
                converted["nullable"] = True
            else:
                converted["type"] = value
        elif key == "properties" and isinstance(value, dict):
            converted["properties"] = {k: _to_gemini_schema(v) for k, v in value.items()}
        elif key == "items" and isinstance(value, dict):
            converted["items"] = _to_gemini_schema(value)
        else:
            converted[key] = value
    return converted
