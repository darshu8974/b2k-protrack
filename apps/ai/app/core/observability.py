"""Prometheus metrics exposure via prometheus-fastapi-instrumentator.

Default HTTP request metrics are exposed at /internal/v1/metrics. Custom metrics
(LLM latency, token usage, parser duration, preflight pass rate) are added in the
sprints that implement those features.
"""

from __future__ import annotations

from fastapi import FastAPI
from prometheus_fastapi_instrumentator import Instrumentator


def setup_observability(app: FastAPI) -> None:
    """Instrument the app and expose the metrics endpoint."""
    Instrumentator().instrument(app).expose(
        app,
        endpoint="/internal/v1/metrics",
        include_in_schema=False,
    )
