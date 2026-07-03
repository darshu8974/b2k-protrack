"""Posts progress callbacks to Spring Boot during a job (relayed to clients via SSE).

The AI service is stateless: it reports progress against a job_id it receives but owns no job
state. Progress posting is **best-effort** — a callback failure (e.g. Spring not reachable during
standalone runs) is logged and never fails the analysis pipeline.
"""

from __future__ import annotations

from app.core.logging import get_logger
from app.schemas.common import JobStatus
from app.schemas.internal import ProgressCallback

logger = get_logger(__name__)


class ProgressReporter:
    def __init__(self, base_url: str, internal_key: str, timeout_ms: int = 5_000) -> None:
        self._base_url = base_url.rstrip("/")
        self._internal_key = internal_key
        self._timeout_s = timeout_ms / 1000

    async def post(
        self,
        job_id: str,
        progress_pct: int,
        status: JobStatus = JobStatus.RUNNING,
        partial: dict | None = None,
    ) -> None:
        callback = ProgressCallback(
            job_id=job_id, progress_pct=progress_pct, status=status, partial=partial
        )
        url = f"{self._base_url}/internal/v1/ai-jobs/{job_id}/progress"
        try:
            import httpx

            async with httpx.AsyncClient(timeout=self._timeout_s) as client:
                await client.post(
                    url,
                    headers={"X-Internal-Key": self._internal_key},
                    json=callback.model_dump(by_alias=True, exclude={"job_id"}),
                )
        except Exception as exc:  # noqa: BLE001 — progress is best-effort, never fatal
            logger.warning(
                "progress_callback_failed", job_id=job_id, pct=progress_pct, error=str(exc)
            )


class NoOpProgressReporter(ProgressReporter):
    """A reporter that records nothing and posts nothing (used in tests)."""

    def __init__(self) -> None:
        super().__init__(base_url="", internal_key="")

    async def post(
        self,
        job_id: str,
        progress_pct: int,
        status: JobStatus = JobStatus.RUNNING,
        partial: dict | None = None,
    ) -> None:
        return None
