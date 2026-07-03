"""Provider abstraction tests: deterministic mock output and config-driven routing."""

from __future__ import annotations

import pytest

from app.providers.claude_provider import ClaudeProvider
from app.providers.mock_provider import MockProvider
from app.providers.router import get_provider

_REQUIRED_KEYS = {
    "overallConfidence",
    "summary",
    "complexityScore",
    "complexityLabel",
    "estimatedWorkingDays",
    "composition",
    "risks",
    "suggestedTeam",
}


async def test_mock_provider_returns_schema_valid_judgement() -> None:
    provider = MockProvider()
    response = await provider.generate_structured(system="s", user="u", output_schema={})

    assert provider.name == "mock"
    assert _REQUIRED_KEYS.issubset(response.data.keys())
    assert 0 <= response.data["overallConfidence"] <= 100
    total = sum(seg["percentage"] for seg in response.data["composition"])
    assert 95 <= total <= 105  # composition sums to ~100
    assert all(r["severity"] in {"HIGH", "MEDIUM", "LOW"} for r in response.data["risks"])


async def test_mock_provider_is_deterministic() -> None:
    a = await MockProvider().generate_structured(system="s", user="u", output_schema={})
    b = await MockProvider().generate_structured(system="s", user="different", output_schema={})
    assert a.data == b.data  # identical shape/values regardless of input


def test_router_defaults_to_mock() -> None:
    # With no AI_PROVIDER set, the default settings select the mock provider.
    provider = get_provider()
    assert isinstance(provider, MockProvider)


def test_claude_provider_requires_api_key() -> None:
    from app.core.errors import PermanentError

    with pytest.raises(PermanentError):
        ClaudeProvider(api_key="", model="claude-sonnet-4-6")
