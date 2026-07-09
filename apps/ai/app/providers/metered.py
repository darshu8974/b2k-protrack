"""Metrics + logging decorator for any :class:`LLMProvider`.

Wraps the active provider so every structured-generation call records latency, token usage,
estimated cost, and error counts (Prometheus) plus a structured timing log line — uniformly across
all orchestrators, without any of them knowing about metrics. Only metadata is recorded/logged
(provider, model, task, token counts, elapsed) — never prompt or document content.
"""

from __future__ import annotations

import time
from typing import Any

from app.core.errors import OutputValidationError, PermanentError, TransientProviderError
from app.core.logging import get_logger
from app.core.metrics import record_llm_call, record_llm_error
from app.providers.base import LLMProvider, LLMResponse

logger = get_logger(__name__)


class MeteredProvider:
    """LLMProvider that instruments a delegate provider with metrics + timing logs."""

    def __init__(self, delegate: LLMProvider) -> None:
        self._delegate = delegate
        self.name = delegate.name

    async def generate_structured(
        self,
        *,
        system: str,
        user: str,
        output_schema: dict[str, Any],
        options: dict[str, Any] | None = None,
    ) -> LLMResponse:
        task = _task_of(output_schema)
        start = time.perf_counter()
        try:
            response = await self._delegate.generate_structured(
                system=system, user=user, output_schema=output_schema, options=options
            )
        except Exception as exc:
            record_llm_error(self.name, _error_kind(exc))
            logger.warning(
                "llm_call_failed", provider=self.name, task=task,
                error_type=type(exc).__name__,
                elapsed_ms=round((time.perf_counter() - start) * 1000, 1),
            )
            raise

        elapsed = time.perf_counter() - start
        record_llm_call(self.name, task, elapsed, response.usage)
        logger.info(
            "llm_call", provider=self.name, model=response.usage.model, task=task,
            input_tokens=response.usage.input_tokens, output_tokens=response.usage.output_tokens,
            elapsed_ms=round(elapsed * 1000, 1),
        )
        return response


def _task_of(output_schema: dict[str, Any]) -> str:
    """Infer the task from the requested output schema (same top-level keys the mock uses)."""
    properties = output_schema.get("properties", {}) if isinstance(output_schema, dict) else {}
    if "reply" in properties:
        return "assistant"
    if "issues" in properties:
        return "preflight"
    return "analysis"


def _error_kind(exc: Exception) -> str:
    if isinstance(exc, TransientProviderError):
        return "transient"
    if isinstance(exc, OutputValidationError):
        return "validation"
    if isinstance(exc, PermanentError):
        return "permanent"
    return "other"
