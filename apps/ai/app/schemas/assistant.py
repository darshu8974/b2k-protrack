"""Scoped assistant chat request/response contract."""

from __future__ import annotations

from pydantic import BaseModel

from app.schemas.internal import LLMUsage, ProjectContext


class AssistantMessage(BaseModel):
    role: str  # "user" | "assistant"
    content: str


class AssistantChatRequest(BaseModel):
    project_context: ProjectContext | None = None
    history: list[AssistantMessage] = []
    message: str


class AssistantReply(BaseModel):
    reply: str
    citations: list[str] | None = None
    usage: LLMUsage | None = None
