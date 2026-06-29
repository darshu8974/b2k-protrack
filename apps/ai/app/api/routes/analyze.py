"""Manuscript analysis route (skeleton). Business logic lands in Sprint 4."""

from __future__ import annotations

from fastapi import APIRouter, Depends, status

from app.core.security import verify_internal_key
from app.schemas.analysis import AnalysisResult, ManuscriptAnalysisRequest

router = APIRouter(
    prefix="/internal/v1",
    tags=["analyze"],
    dependencies=[Depends(verify_internal_key)],
)


@router.post("/analyze/manuscript", response_model=AnalysisResult)
async def analyze_manuscript(request: ManuscriptAnalysisRequest) -> AnalysisResult:
    from fastapi import HTTPException

    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="Manuscript analysis is implemented in Sprint 4",
    )
