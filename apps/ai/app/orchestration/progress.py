"""Posts progress callbacks to Spring Boot during long jobs (relayed to clients via SSE).

The AI service is stateless: it reports progress against a job_id it receives, but owns no job
state. Implemented in Sprint 4. httpx is imported lazily.
"""

from __future__ import annotations

from app.schemas.common import JobStatus
from app.schemas.internal import ProgressCallback


class ProgressReporter:
    def __init__(self, base_url: str, internal_key: str) -> None:
        self._base_url = base_url
        self._internal_key = internal_key

    async def post(
        self, job_id: str, progress_pct: int, status: JobStatus, partial: dict | None = None
    ) -> None:
        _ = ProgressCallback(
            job_id=job_id, progress_pct=progress_pct, status=status, partial=partial
        )
        raise NotImplementedError("Progress callbacks are implemented in Sprint 4")
