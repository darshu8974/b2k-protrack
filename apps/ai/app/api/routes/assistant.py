"""Scoped assistant chat route (skeleton). Business logic lands in Sprint 6."""

from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status

from app.core.security import verify_internal_key
from app.schemas.assistant import AssistantChatRequest, AssistantReply

router = APIRouter(
    prefix="/internal/v1",
    tags=["assistant"],
    dependencies=[Depends(verify_internal_key)],
)


@router.post("/assistant/chat", response_model=AssistantReply)
async def assistant_chat(request: AssistantChatRequest) -> AssistantReply:
    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="Assistant chat is implemented in Sprint 6",
    )
