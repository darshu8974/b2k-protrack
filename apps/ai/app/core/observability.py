"""Prometheus metrics exposure via prometheus-fastapi-instrumentator.

The instrumentator exposes default HTTP request metrics at /internal/v1/metrics. The service's
custom metrics (LLM latency / token usage / estimated cost / errors, retries, pipeline step
durations, preflight pass rate) live in ``app.core.metrics`` and register on the same default
Prometheus registry, so they are scraped from the same endpoint. Importing the metrics module here
guarantees they are registered even before their first call site runs.
"""

from __future__ import annotations

from fastapi import FastAPI
from prometheus_fastapi_instrumentator import Instrumentator

from app.core import metrics as _metrics  # noqa: F401 — ensure custom metrics are registered


def setup_observability(app: FastAPI) -> None:
    """Instrument the app and expose the metrics endpoint (HTTP + custom AI metrics)."""
    Instrumentator().instrument(app).expose(
        app,
        endpoint="/internal/v1/metrics",
        include_in_schema=False,
    )
