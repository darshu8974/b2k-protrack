"""Linear pipeline runner.

Orchestrators compose ordered steps (load -> parse -> build_prompt -> generate -> normalize).
Each step receives the accumulated context and returns updates to merge in; after each step the
pipeline emits a progress milestone (best-effort) and records the step's duration (Prometheus +
a structured timing log — metadata only, never document content). The runner is transport-agnostic
— the orchestrator wires in a ProgressReporter, job id, and a task label for the metrics.
"""

from __future__ import annotations

import time
from collections.abc import Awaitable, Callable
from dataclasses import dataclass
from typing import Any

from app.core.logging import get_logger
from app.core.metrics import PIPELINE_STEP
from app.orchestration.progress import ProgressReporter
from app.schemas.common import JobStatus

logger = get_logger(__name__)

Step = Callable[[dict[str, Any]], Awaitable[dict[str, Any]]]


@dataclass(frozen=True)
class PipelineStep:
    name: str
    progress_pct: int
    run: Step


class Pipeline:
    def __init__(
        self,
        steps: list[PipelineStep],
        *,
        reporter: ProgressReporter | None = None,
        job_id: str | None = None,
        task: str = "unknown",
    ) -> None:
        self._steps = steps
        self._reporter = reporter
        self._job_id = job_id
        self._task = task

    async def run(self, initial: dict[str, Any]) -> dict[str, Any]:
        context: dict[str, Any] = dict(initial)
        for step in self._steps:
            start = time.perf_counter()
            updates = await step.run(context)
            elapsed = time.perf_counter() - start
            PIPELINE_STEP.labels(task=self._task, step=step.name).observe(elapsed)
            logger.info(
                "pipeline_step", task=self._task, step=step.name,
                elapsed_ms=round(elapsed * 1000, 1),
            )
            if updates:
                context.update(updates)
            await self._report(step.progress_pct)
        return context

    async def _report(self, pct: int) -> None:
        if self._reporter is not None and self._job_id is not None:
            await self._reporter.post(self._job_id, pct, JobStatus.RUNNING)
