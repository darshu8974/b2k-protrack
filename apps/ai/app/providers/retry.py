"""Retry policy for LLM/provider calls (tenacity).

Exponential backoff with jitter, bounded attempts, retrying only transient errors. Each retry
attempt is counted (Prometheus) via the ``before_sleep`` hook, labelled by the triggering
exception, so retry pressure is visible on dashboards.
"""

from __future__ import annotations

from tenacity import (
    retry,
    retry_if_exception_type,
    stop_after_attempt,
    wait_exponential_jitter,
)

from app.core.errors import DownstreamError, TransientProviderError
from app.core.metrics import record_retry


def _count_retry(retry_state: object) -> None:
    """tenacity before_sleep hook: increment the retry counter, labelled by exception class."""
    outcome = getattr(retry_state, "outcome", None)
    exc = outcome.exception() if outcome is not None else None
    record_retry(type(exc).__name__ if exc is not None else "unknown")


llm_retry = retry(
    reraise=True,
    stop=stop_after_attempt(4),
    wait=wait_exponential_jitter(initial=0.5, max=8.0),
    retry=retry_if_exception_type((TransientProviderError, DownstreamError)),
    before_sleep=_count_retry,
)
