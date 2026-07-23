"""Provider abstraction tests: deterministic mock output and config-driven routing."""

from __future__ import annotations

import pytest

from app.providers import router
from app.providers.claude_provider import ClaudeProvider
from app.providers.gemini_provider import GeminiProvider, _to_gemini_schema
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


async def test_mock_provider_returns_preflight_phrasing_for_preflight_schema() -> None:
    # The mock infers the task from the output schema (preflight schema has a top-level `issues`).
    schema = {"properties": {"issues": {"type": "array"}}}
    response = await MockProvider().generate_structured(system="s", user="u", output_schema=schema)

    assert "issues" in response.data
    assert "overallConfidence" not in response.data  # not the analysis payload
    keys = {issue["checkKey"] for issue in response.data["issues"]}
    assert "font_embedding" in keys
    assert all(i["severity"] in {"HIGH", "MEDIUM", "LOW"} for i in response.data["issues"])


def test_router_defaults_to_mock() -> None:
    # With no AI_PROVIDER set, the default settings select the mock provider.
    provider = get_provider()
    assert isinstance(provider, MockProvider)


def test_claude_provider_requires_api_key() -> None:
    from app.core.errors import PermanentError

    with pytest.raises(PermanentError):
        ClaudeProvider(api_key="", model="claude-sonnet-4-6")


def test_gemini_provider_requires_api_key() -> None:
    from app.core.errors import PermanentError

    with pytest.raises(PermanentError):
        GeminiProvider(api_key="", model="gemini-2.5-flash")


def test_router_selects_gemini_when_configured(monkeypatch: pytest.MonkeyPatch) -> None:
    class _FakeSettings:
        llm_provider = "gemini"
        gemini_api_key = "test-key"
        gemini_model = "gemini-2.5-flash"
        llm_max_tokens = 4096
        llm_temperature = 0.2

    monkeypatch.setattr(router, "get_settings", lambda: _FakeSettings())
    provider = get_provider()

    assert isinstance(provider, GeminiProvider)
    assert provider.name == "gemini"


def test_router_rejects_unsupported_provider(monkeypatch: pytest.MonkeyPatch) -> None:
    from app.core.errors import PermanentError

    class _FakeSettings:
        llm_provider = "not-a-real-provider"

    monkeypatch.setattr(router, "get_settings", lambda: _FakeSettings())
    with pytest.raises(PermanentError):
        get_provider()


class TestGeminiSchemaConversion:
    """The private nullable-union -> OpenAPI-nullable conversion, isolated from the network."""

    def test_converts_top_level_nullable_union(self) -> None:
        schema = {"type": ["string", "null"]}
        assert _to_gemini_schema(schema) == {"type": "string", "nullable": True}

    def test_leaves_non_nullable_types_unchanged(self) -> None:
        schema = {"type": "integer", "minimum": 0, "maximum": 100}
        assert _to_gemini_schema(schema) == schema

    def test_converts_nullable_unions_inside_nested_properties(self) -> None:
        schema = {
            "type": "object",
            "properties": {
                "language": {"type": ["string", "null"]},
                "score": {"type": "integer"},
            },
        }
        converted = _to_gemini_schema(schema)
        assert converted["properties"]["language"] == {"type": "string", "nullable": True}
        assert converted["properties"]["score"] == {"type": "integer"}

    def test_converts_nullable_unions_inside_array_items(self) -> None:
        schema = {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {"candidateHint": {"type": ["string", "null"]}},
            },
        }
        converted = _to_gemini_schema(schema)
        item_props = converted["items"]["properties"]
        assert item_props["candidateHint"] == {"type": "string", "nullable": True}

    def test_leaves_multi_type_unions_that_are_not_simple_nullables_unchanged(self) -> None:
        # A union with more than one non-null type isn't the simple nullable-field pattern this
        # helper targets — pass it through rather than guessing which type Gemini should use.
        schema = {"type": ["string", "integer", "null"]}
        assert _to_gemini_schema(schema) == schema

    def test_converts_the_real_manuscript_analysis_schema_with_no_remaining_type_arrays(
        self,
    ) -> None:
        from app.prompts.registry import MANUSCRIPT_ANALYSIS_V1, PromptRegistry

        schema = PromptRegistry().output_schema(MANUSCRIPT_ANALYSIS_V1)
        converted = _to_gemini_schema(schema)

        assert converted["properties"]["language"] == {
            "type": "string",
            "nullable": True,
            "description": schema["properties"]["language"]["description"],
        }
        assert _no_type_arrays_remain(converted)


def _no_type_arrays_remain(node: object) -> bool:
    """Recursively assert no ``"type"`` key is still a list anywhere in the converted schema."""
    if isinstance(node, dict):
        if isinstance(node.get("type"), list):
            return False
        return all(_no_type_arrays_remain(v) for v in node.values())
    if isinstance(node, list):
        return all(_no_type_arrays_remain(v) for v in node)
    return True
