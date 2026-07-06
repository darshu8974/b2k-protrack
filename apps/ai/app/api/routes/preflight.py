"""PDF preflight route: run deterministic checks + LLM phrasing into a normalized PreflightResult.

Runs the preflight pipeline inline (async) and returns the normalized result; Spring Boot persists
it 1:1 into preflight_runs / preflight_checks / qa_issues. Authenticated with the internal key.
Failures surface as a structured ErrorPayload via the AiServiceError handlers.
"""

from __future__ import annotations

from fastapi import APIRouter, Depends

from app.api.deps import preflight_orchestrator_dep
from app.core.security import verify_internal_key
from app.orchestration.preflight_orchestrator import PreflightOrchestrator
from app.schemas.preflight import PdfPreflightRequest, PreflightResult

router = APIRouter(
    prefix="/internal/v1",
    tags=["preflight"],
    dependencies=[Depends(verify_internal_key)],
)


@router.post("/preflight/pdf", response_model=PreflightResult)
async def preflight_pdf(
    request: PdfPreflightRequest,
    orchestrator: PreflightOrchestrator = Depends(preflight_orchestrator_dep),
) -> PreflightResult:
    return await orchestrator.run(request)
