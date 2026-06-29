"""PDF preflight route (skeleton). Business logic lands in Sprint 5."""

from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status

from app.core.security import verify_internal_key
from app.schemas.preflight import PdfPreflightRequest, PreflightResult

router = APIRouter(
    prefix="/internal/v1",
    tags=["preflight"],
    dependencies=[Depends(verify_internal_key)],
)


@router.post("/preflight/pdf", response_model=PreflightResult)
async def preflight_pdf(request: PdfPreflightRequest) -> PreflightResult:
    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="PDF preflight is implemented in Sprint 5",
    )
