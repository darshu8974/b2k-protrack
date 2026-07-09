"""Custom Prometheus metrics for the AI service (LLM latency/tokens/cost/errors, retries, pipeline
step durations, preflight pass rate).

Metrics register on the default Prometheus registry, so they are exposed by the same
``/internal/v1/metrics`` endpoint as the instrumentator's HTTP metrics (see observability.py).
Labels stay low-cardinality (provider / model / task / step) — never per-user or per-document. The
cost counter uses a static per-model estimate table (below); it is a monitoring signal, not a bill.
"""

from __future__ import annotations

from prometheus_client import Counter, Histogram

from app.providers.base import Usage

_NS = "protrack_ai"

# ── LLM call metrics ─────────────────────────────────────────────────────────
LLM_LATENCY = Histogram(
    f"{_NS}_llm_call_seconds", "LLM structured-generation call latency",
    labelnames=("provider", "model", "task"),
)
LLM_TOKENS = Counter(
    f"{_NS}_llm_tokens_total", "LLM tokens consumed",
    labelnames=("provider", "model", "direction"),
)
LLM_COST_USD = Counter(
    f"{_NS}_llm_cost_usd_total", "Estimated LLM cost in USD (static price table)",
    labelnames=("provider", "model"),
)
LLM_ERRORS = Counter(
    f"{_NS}_llm_errors_total", "LLM call failures by error kind",
    labelnames=("provider", "kind"),
)
LLM_RETRIES = Counter(
    f"{_NS}_llm_retries_total", "LLM call retry attempts by reason",
    labelnames=("reason",),
)

# ── Pipeline / preflight metrics ─────────────────────────────────────────────
PIPELINE_STEP = Histogram(
    f"{_NS}_pipeline_step_seconds", "Orchestration step duration (parse/generate/normalize/...)",
    labelnames=("task", "step"),
)
PREFLIGHT_RUNS = Counter(
    f"{_NS}_preflight_runs_total", "Completed preflight runs by outcome",
    labelnames=("passed",),
)

# Estimated USD price per 1K tokens (input, output), matched by a substring of the model id.
# Estimates for monitoring/cost-awareness only — tune to your provider contract. Unknown/mock -> 0.
_PRICE_PER_1K: dict[str, tuple[float, float]] = {
    "claude": (0.003, 0.015),  # Claude Sonnet-class estimate
}


def estimate_cost_usd(model: str | None, input_tokens: int, output_tokens: int) -> float:
    """Estimate the USD cost of a call from a static per-model price table (0 if unknown/mock)."""
    if not model:
        return 0.0
    lowered = model.lower()
    for key, (in_rate, out_rate) in _PRICE_PER_1K.items():
        if key in lowered:
            return (input_tokens / 1000.0) * in_rate + (output_tokens / 1000.0) * out_rate
    return 0.0


def record_llm_call(provider: str, task: str, elapsed_seconds: float, usage: Usage) -> None:
    """Record latency, token usage, and estimated cost for one successful LLM call."""
    model = usage.model or provider
    LLM_LATENCY.labels(provider=provider, model=model, task=task).observe(elapsed_seconds)
    LLM_TOKENS.labels(provider=provider, model=model, direction="input").inc(usage.input_tokens)
    LLM_TOKENS.labels(provider=provider, model=model, direction="output").inc(usage.output_tokens)
    cost = estimate_cost_usd(model, usage.input_tokens, usage.output_tokens)
    if cost:
        LLM_COST_USD.labels(provider=provider, model=model).inc(cost)


def record_llm_error(provider: str, kind: str) -> None:
    """Record a failed LLM call, labelled by error kind (transient/permanent/validation/other)."""
    LLM_ERRORS.labels(provider=provider, kind=kind).inc()


def record_retry(reason: str) -> None:
    """Record one LLM retry attempt, labelled by the triggering exception class name."""
    LLM_RETRIES.labels(reason=reason).inc()


def record_preflight_outcome(passed: bool) -> None:
    """Record a completed preflight run's pass/fail outcome."""
    PREFLIGHT_RUNS.labels(passed=str(passed).lower()).inc()
