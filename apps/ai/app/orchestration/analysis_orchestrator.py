"""Orchestrates manuscript analysis: load -> parse -> build_prompt -> generate -> normalize,
emitting progress callbacks between steps.

Parsers produce deterministic facts (off the event loop via a thread pool); the provider produces
judgement; the normalizer merges and validates. A single bounded "repair" retry re-prompts when the
model's output fails schema validation. The service stays stateless — progress is reported against
the caller's job id, nothing is persisted here.
"""

from __future__ import annotations

from typing import Any

from starlette.concurrency import run_in_threadpool

from app.core.errors import OutputValidationError
from app.orchestration.pipeline import Pipeline, PipelineStep
from app.orchestration.progress import ProgressReporter
from app.parsers.factory import get_parser
from app.prompts.registry import MANUSCRIPT_ANALYSIS_V1, PromptRegistry
from app.providers.base import LLMProvider
from app.schemas.analysis import AnalysisResult, ManuscriptAnalysisRequest
from app.schemas.common import JobStatus
from app.schemas.internal import ProjectContext
from app.services.normalizer import AnalysisNormalizer

_REPAIR_HINT = (
    "\n\nYour previous response did not match the required schema. "
    "Return ONLY the structured tool output with all required fields present and valid."
)


class AnalysisOrchestrator:
    def __init__(
        self,
        *,
        provider: LLMProvider,
        file_loader: Any,
        prompt_registry: PromptRegistry,
        normalizer: AnalysisNormalizer,
        reporter: ProgressReporter,
        prompt_id: str = MANUSCRIPT_ANALYSIS_V1,
    ) -> None:
        self._provider = provider
        self._file_loader = file_loader
        self._registry = prompt_registry
        self._normalizer = normalizer
        self._reporter = reporter
        self._prompt_id = prompt_id

    async def run(self, request: ManuscriptAnalysisRequest) -> AnalysisResult:
        await self._reporter.post(request.job_id, 5, JobStatus.RUNNING)

        steps = [
            PipelineStep("load", 5, self._load),
            PipelineStep("parse", 25, self._parse),
            PipelineStep("build_prompt", 40, self._build_prompt),
            PipelineStep("generate", 75, self._generate),
            PipelineStep("normalize", 95, self._normalize),
        ]
        pipeline = Pipeline(steps, reporter=self._reporter, job_id=request.job_id)
        context = await pipeline.run({"request": request})
        result: AnalysisResult = context["result"]

        await self._reporter.post(
            request.job_id, 100, JobStatus.RUNNING, partial={"summary": result.summary}
        )
        return result

    async def _load(self, context: dict[str, Any]) -> dict[str, Any]:
        request: ManuscriptAnalysisRequest = context["request"]
        return {"content": await self._file_loader.fetch(request.file_url)}

    async def _parse(self, context: dict[str, Any]) -> dict[str, Any]:
        request: ManuscriptAnalysisRequest = context["request"]
        parser = get_parser(request.doc_type)
        parsed = await run_in_threadpool(parser.parse, context["content"])
        return {"parsed": parsed}

    async def _build_prompt(self, context: dict[str, Any]) -> dict[str, Any]:
        request: ManuscriptAnalysisRequest = context["request"]
        parsed = context["parsed"]
        project = request.project_context or ProjectContext(project_id="unknown")
        prompt_context = {
            "project": project,
            "counts": parsed.counts,
            "headings": parsed.headings,
            "language": parsed.language,
            "sample": parsed.raw_text_sample,
        }
        return {
            "system": self._registry.render_system(self._prompt_id),
            "user": self._registry.render_user(self._prompt_id, prompt_context),
            "schema": self._registry.output_schema(self._prompt_id),
        }

    async def _generate(self, context: dict[str, Any]) -> dict[str, Any]:
        llm = await self._provider.generate_structured(
            system=context["system"], user=context["user"], output_schema=context["schema"]
        )
        return {"llm": llm}

    async def _normalize(self, context: dict[str, Any]) -> dict[str, Any]:
        parsed = context["parsed"]
        try:
            result = self._normalizer.normalize(parsed, context["llm"], prompt_id=self._prompt_id)
        except OutputValidationError:
            # Bounded repair: re-prompt once with the validation hint, then re-normalize.
            repaired = await self._provider.generate_structured(
                system=context["system"],
                user=context["user"] + _REPAIR_HINT,
                output_schema=context["schema"],
            )
            result = self._normalizer.normalize(parsed, repaired, prompt_id=self._prompt_id)
        return {"result": result}
