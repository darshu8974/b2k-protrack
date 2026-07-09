"""Unit tests for the custom Prometheus metrics helpers (app.core.metrics)."""

from __future__ import annotations

from prometheus_client import REGISTRY

from app.core.metrics import (
    estimate_cost_usd,
    record_llm_call,
    record_preflight_outcome,
    record_retry,
)
from app.providers.base import Usage


def _sample(name: str, labels: dict[str, str]) -> float:
    value = REGISTRY.get_sample_value(name, labels)
    return value if value is not None else 0.0


def test_estimate_cost_uses_the_price_table_for_known_models() -> None:
    # Claude Sonnet estimate: 0.003/1k in + 0.015/1k out.
    assert estimate_cost_usd("claude-sonnet-4-6", 1000, 1000) == 0.018
    assert estimate_cost_usd("claude-sonnet-4-6", 2000, 0) == 0.006


def test_estimate_cost_is_zero_for_mock_and_unknown_models() -> None:
    assert estimate_cost_usd("mock", 1000, 1000) == 0.0
    assert estimate_cost_usd("some-other-model", 1000, 1000) == 0.0
    assert estimate_cost_usd(None, 1000, 1000) == 0.0


def test_record_llm_call_increments_tokens_and_cost() -> None:
    model = "claude-sonnet-4-6"
    tin = {"provider": "claude", "model": model, "direction": "input"}
    tout = {"provider": "claude", "model": model, "direction": "output"}
    clabels = {"provider": "claude", "model": model}
    before_in = _sample("protrack_ai_llm_tokens_total", tin)
    before_out = _sample("protrack_ai_llm_tokens_total", tout)
    before_cost = _sample("protrack_ai_llm_cost_usd_total", clabels)
    before_calls = _sample("protrack_ai_llm_call_seconds_count",
                           {"provider": "claude", "model": model, "task": "analysis"})

    usage = Usage(input_tokens=100, output_tokens=200, model=model)
    record_llm_call("claude", "analysis", 0.25, usage)

    assert _sample("protrack_ai_llm_tokens_total", tin) == before_in + 100
    assert _sample("protrack_ai_llm_tokens_total", tout) == before_out + 200
    assert _sample("protrack_ai_llm_cost_usd_total", clabels) > before_cost  # cost accrued
    assert _sample("protrack_ai_llm_call_seconds_count",
                   {"provider": "claude", "model": model, "task": "analysis"}) == before_calls + 1


def test_record_preflight_outcome_labels_pass_and_fail() -> None:
    before = _sample("protrack_ai_preflight_runs_total", {"passed": "true"})
    record_preflight_outcome(True)
    assert _sample("protrack_ai_preflight_runs_total", {"passed": "true"}) == before + 1


def test_record_retry_increments_by_reason() -> None:
    labels = {"reason": "TransientProviderError"}
    before = _sample("protrack_ai_llm_retries_total", labels)
    record_retry("TransientProviderError")
    assert _sample("protrack_ai_llm_retries_total", labels) == before + 1
