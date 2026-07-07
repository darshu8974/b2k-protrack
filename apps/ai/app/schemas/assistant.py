"""Scoped assistant chat request/response contract (camelCase wire form per the API spec)."""

from __future__ import annotations

from app.schemas.common import CamelModel
from app.schemas.internal import LLMUsage, ProjectContext


class AssistantMessage(CamelModel):
    """One turn of prior conversation history (role is "user" or "assistant")."""

    role: str
    content: str


class AssistantChatRequest(CamelModel):
    """A scoped question plus optional project context and prior turns (RAG-lite)."""

    message: str
    project_context: ProjectContext | None = None
    history: list[AssistantMessage] = []


class AssistantReply(CamelModel):
    """The assistant's answer. ``citations`` optionally references the provided context."""

    reply: str
    citations: list[str] | None = None
    usage: LLMUsage | None = None
