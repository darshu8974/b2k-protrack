"""Orchestrates the scoped assistant chat (RAG-lite over provided project context).
Implemented in Sprint 6."""

from __future__ import annotations

from app.schemas.assistant import AssistantChatRequest, AssistantReply


class AssistantOrchestrator:
    async def run(self, request: AssistantChatRequest) -> AssistantReply:
        raise NotImplementedError("Assistant chat is implemented in Sprint 6")
