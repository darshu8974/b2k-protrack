"""Retry policy for LLM/provider calls (tenacity).

Exponential backoff with jitter, bounded attempts, retrying only transient errors. Applied to
provider calls in the sprints that implement them.
"""

from __future__ import annotations

from tenacity import (
    retry,
    retry_if_exception_type,
    stop_after_attempt,
    wait_exponential_jitter,
)

from app.core.errors import DownstreamError, TransientProviderError

llm_retry = retry(
    reraise=True,
    stop=stop_after_attempt(4),
    wait=wait_exponential_jitter(initial=0.5, max=8.0),
    retry=retry_if_exception_type((TransientProviderError, DownstreamError)),
)
