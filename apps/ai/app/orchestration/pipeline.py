"""Linear pipeline runner.

Orchestrators compose ordered steps (load -> parse -> build_prompt -> generate -> normalize).
Each step receives the accumulated context and returns updates to merge in; after each step the
pipeline emits a progress milestone (best-effort). The runner is transport-agnostic — the
orchestrator wires in a ProgressReporter and job id.
"""

from __future__ import annotations

from collections.abc import Awaitable, Callable
from dataclasses import dataclass
from typing import Any

from app.orchestration.progress import ProgressReporter
from app.schemas.common import JobStatus

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
    ) -> None:
        self._steps = steps
        self._reporter = reporter
        self._job_id = job_id

    async def run(self, initial: dict[str, Any]) -> dict[str, Any]:
        context: dict[str, Any] = dict(initial)
        for step in self._steps:
            updates = await step.run(context)
            if updates:
                context.update(updates)
            await self._report(step.progress_pct)
        return context

    async def _report(self, pct: int) -> None:
        if self._reporter is not None and self._job_id is not None:
            await self._reporter.post(self._job_id, pct, JobStatus.RUNNING)
