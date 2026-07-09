"""Unit tests for the MeteredProvider instrumentation decorator."""

from __future__ import annotations

from typing import Any

import pytest
from prometheus_client import REGISTRY

from app.core.errors import TransientProviderError
from app.providers.base import LLMResponse, Usage
from app.providers.metered import MeteredProvider


def _sample(name: str, labels: dict[str, str]) -> float:
    value = REGISTRY.get_sample_value(name, labels)
    return value if value is not None else 0.0


class _FakeProvider:
    name = "fake"

    def __init__(self, response: LLMResponse | None = None, exc: Exception | None = None) -> None:
        self._response = response
        self._exc = exc
        self.calls = 0

    async def generate_structured(self, **_: Any) -> LLMResponse:
        self.calls += 1
        if self._exc is not None:
            raise self._exc
        assert self._response is not None
        return self._response


async def test_delegates_and_records_a_successful_call() -> None:
    usage = Usage(input_tokens=5, output_tokens=7, model="fake")
    response = LLMResponse(data={"reply": "hi"}, usage=usage)
    provider = MeteredProvider(_FakeProvider(response=response))
    before = _sample("protrack_ai_llm_call_seconds_count",
                     {"provider": "fake", "model": "fake", "task": "assistant"})

    result = await provider.generate_structured(
        system="s", user="u", output_schema={"properties": {"reply": {}}})

    assert result is response
    assert provider.name == "fake"
    assert _sample("protrack_ai_llm_call_seconds_count",
                   {"provider": "fake", "model": "fake", "task": "assistant"}) == before + 1


async def test_records_error_and_reraises() -> None:
    provider = MeteredProvider(_FakeProvider(exc=TransientProviderError("boom")))
    before = _sample("protrack_ai_llm_errors_total", {"provider": "fake", "kind": "transient"})

    with pytest.raises(TransientProviderError):
        await provider.generate_structured(system="s", user="u", output_schema={})

    assert _sample("protrack_ai_llm_errors_total",
                   {"provider": "fake", "kind": "transient"}) == before + 1
