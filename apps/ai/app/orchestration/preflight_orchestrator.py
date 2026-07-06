"""Orchestrates PDF preflight: load -> parse -> checks -> build_prompt -> phrase -> normalize,
emitting progress callbacks between steps.

Deterministic checks (run off the event loop via a thread pool) decide PASS/REVIEW/FAIL, the
overall score, and which issues exist — the source of truth. The provider only phrases each
finding's severity/title/recommendation; when there is nothing to phrase (a clean PDF) the LLM is
skipped. The service stays stateless — progress is reported against the caller's job id; nothing is
persisted.
"""

from __future__ import annotations

from typing import Any

from starlette.concurrency import run_in_threadpool

from app.orchestration.pipeline import Pipeline, PipelineStep
from app.orchestration.progress import ProgressReporter
from app.parsers.pdf_parser import extract_pdf_facts
from app.preflight.models import CheckOutcome
from app.preflight.runner import PreflightRunner
from app.prompts.registry import PREFLIGHT_FINDINGS_V1, PromptRegistry
from app.providers.base import LLMProvider, LLMResponse, Usage
from app.schemas.common import JobStatus
from app.schemas.preflight import PdfPreflightRequest, PreflightResult
from app.services.preflight_normalizer import PreflightNormalizer


class PreflightOrchestrator:
    def __init__(
        self,
        *,
        provider: LLMProvider,
        file_loader: Any,
        prompt_registry: PromptRegistry,
        runner: PreflightRunner,
        normalizer: PreflightNormalizer,
        reporter: ProgressReporter,
        prompt_id: str = PREFLIGHT_FINDINGS_V1,
    ) -> None:
        self._provider = provider
        self._file_loader = file_loader
        self._registry = prompt_registry
        self._runner = runner
        self._normalizer = normalizer
        self._reporter = reporter
        self._prompt_id = prompt_id

    async def run(self, request: PdfPreflightRequest) -> PreflightResult:
        await self._reporter.post(request.job_id, 5, JobStatus.RUNNING)

        steps = [
            PipelineStep("load", 5, self._load),
            PipelineStep("parse", 25, self._parse),
            PipelineStep("check", 40, self._check),
            PipelineStep("build_prompt", 55, self._build_prompt),
            PipelineStep("phrase", 85, self._phrase),
            PipelineStep("normalize", 95, self._normalize),
        ]
        pipeline = Pipeline(steps, reporter=self._reporter, job_id=request.job_id)
        context = await pipeline.run({"request": request})
        result: PreflightResult = context["result"]

        await self._reporter.post(
            request.job_id,
            100,
            JobStatus.RUNNING,
            partial={"passed": result.passed, "overallScore": result.overall_score},
        )
        return result

    async def _load(self, context: dict[str, Any]) -> dict[str, Any]:
        request: PdfPreflightRequest = context["request"]
        return {"content": await self._file_loader.fetch(request.file_url)}

    async def _parse(self, context: dict[str, Any]) -> dict[str, Any]:
        return {"facts": await run_in_threadpool(extract_pdf_facts, context["content"])}

    async def _check(self, context: dict[str, Any]) -> dict[str, Any]:
        request: PdfPreflightRequest = context["request"]
        outcomes = await run_in_threadpool(self._runner.run, context["facts"], request.standard)
        return {"outcomes": outcomes}

    async def _build_prompt(self, context: dict[str, Any]) -> dict[str, Any]:
        request: PdfPreflightRequest = context["request"]
        findings = [outcome.finding for outcome in context["outcomes"] if outcome.finding]
        prompt_context = {
            "standard": request.standard,
            "findings": [
                {
                    "checkKey": finding.check_key,
                    "result": finding.result.value,
                    "category": finding.category,
                    "pageRef": finding.page_ref,
                    "evidence": finding.evidence,
                }
                for finding in findings
            ],
        }
        return {
            "has_findings": bool(findings),
            "system": self._registry.render_system(self._prompt_id),
            "user": self._registry.render_user(self._prompt_id, prompt_context),
            "schema": self._registry.output_schema(self._prompt_id),
        }

    async def _phrase(self, context: dict[str, Any]) -> dict[str, Any]:
        # Nothing failed -> no phrasing needed; skip the provider call entirely.
        if not context["has_findings"]:
            empty = LLMResponse(data={"issues": []}, usage=Usage(model=self._provider.name))
            return {"llm": empty}
        llm = await self._provider.generate_structured(
            system=context["system"], user=context["user"], output_schema=context["schema"]
        )
        return {"llm": llm}

    async def _normalize(self, context: dict[str, Any]) -> dict[str, Any]:
        request: PdfPreflightRequest = context["request"]
        outcomes: list[CheckOutcome] = context["outcomes"]
        result = self._normalizer.normalize(
            outcomes,
            context["llm"],
            standard=request.standard,
            prompt_id=self._prompt_id,
        )
        return {"result": result}
