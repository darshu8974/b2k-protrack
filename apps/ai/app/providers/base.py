"""LLM provider abstraction.

A provider turns a (system, user, output_schema) request into validated structured data plus
usage. Adapters (Claude and Gemini today; OpenAI later) implement this Protocol so orchestration
and prompts are provider-agnostic.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Protocol, runtime_checkable


@dataclass
class Usage:
    input_tokens: int = 0
    output_tokens: int = 0
    model: str | None = None


@dataclass
class LLMResponse:
    data: dict[str, Any]
    usage: Usage
    raw: Any = field(default=None)


@runtime_checkable
class LLMProvider(Protocol):
    """Provider-neutral structured-generation interface."""

    name: str

    async def generate_structured(
        self,
        *,
        system: str,
        user: str,
        output_schema: dict[str, Any],
        options: dict[str, Any] | None = None,
    ) -> LLMResponse:
        """Return schema-valid structured data for the given prompt."""
        ...
