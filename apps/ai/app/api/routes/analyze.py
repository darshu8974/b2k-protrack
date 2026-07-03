"""Manuscript analysis route: parse + structure a manuscript into a normalized AnalysisResult.

Runs the analysis pipeline inline (async) and returns the normalized result; Spring Boot persists
it 1:1. Authenticated with the internal key. Failures surface as structured ErrorPayload via the
AiServiceError handlers.
"""

from __future__ import annotations

from fastapi import APIRouter, Depends

from app.api.deps import analysis_orchestrator_dep
from app.core.security import verify_internal_key
from app.orchestration.analysis_orchestrator import AnalysisOrchestrator
from app.schemas.analysis import AnalysisResult, ManuscriptAnalysisRequest

router = APIRouter(
    prefix="/internal/v1",
    tags=["analyze"],
    dependencies=[Depends(verify_internal_key)],
)


@router.post("/analyze/manuscript", response_model=AnalysisResult)
async def analyze_manuscript(
    request: ManuscriptAnalysisRequest,
    orchestrator: AnalysisOrchestrator = Depends(analysis_orchestrator_dep),
) -> AnalysisResult:
    return await orchestrator.run(request)
