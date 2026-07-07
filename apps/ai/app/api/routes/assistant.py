"""Scoped assistant chat route: answer a project-scoped question via the active LLM provider.

Runs a single synchronous turn (build prompt -> generate -> reply) and returns the structured
AssistantReply; Spring Boot persists the message. Authenticated with the internal key. Failures
surface as a structured ErrorPayload via the AiServiceError handlers.
"""

from __future__ import annotations

from fastapi import APIRouter, Depends

from app.api.deps import assistant_orchestrator_dep
from app.core.security import verify_internal_key
from app.orchestration.assistant_orchestrator import AssistantOrchestrator
from app.schemas.assistant import AssistantChatRequest, AssistantReply

router = APIRouter(
    prefix="/internal/v1",
    tags=["assistant"],
    dependencies=[Depends(verify_internal_key)],
)


@router.post("/assistant/chat", response_model=AssistantReply)
async def assistant_chat(
    request: AssistantChatRequest,
    orchestrator: AssistantOrchestrator = Depends(assistant_orchestrator_dep),
) -> AssistantReply:
    return await orchestrator.run(request)
