"""Orchestrates the scoped assistant chat (RAG-lite over provided project context).

Unlike the analysis/preflight verticals this is a single synchronous turn: build the prompt from the
provided project context + conversation history, ask the provider for a structured reply, and return
it. Nothing is persisted (Spring owns the thread/messages) and there are no progress callbacks — a
chat turn is fast. A single bounded "repair" retry re-prompts if the model omits the reply. The
provider abstraction is reused unchanged; the forced-tool identity is passed via ``options`` so the
Claude adapter emits a correctly-named tool.
"""

from __future__ import annotations

from app.core.errors import OutputValidationError
from app.prompts.registry import ASSISTANT_V1, PromptRegistry
from app.providers.base import LLMProvider
from app.schemas.assistant import AssistantChatRequest, AssistantReply
from app.schemas.internal import LLMUsage

_TOOL_NAME = "emit_assistant_reply"
_TOOL_DESCRIPTION = "Return the scoped assistant reply matching the schema."
_REPAIR_HINT = (
    "\n\nYour previous response did not match the required schema. "
    "Return ONLY the structured tool output with a non-empty 'reply' field."
)


class AssistantOrchestrator:
    def __init__(
        self,
        *,
        provider: LLMProvider,
        prompt_registry: PromptRegistry,
        prompt_id: str = ASSISTANT_V1,
    ) -> None:
        self._provider = provider
        self._registry = prompt_registry
        self._prompt_id = prompt_id

    async def run(self, request: AssistantChatRequest) -> AssistantReply:
        system = self._registry.render_system(self._prompt_id)
        user = self._registry.render_user(
            self._prompt_id,
            {
                "project": request.project_context,
                "history": request.history,
                "message": request.message,
            },
        )
        schema = self._registry.output_schema(self._prompt_id)
        options = {"tool_name": _TOOL_NAME, "tool_description": _TOOL_DESCRIPTION}

        llm = await self._provider.generate_structured(
            system=system, user=user, output_schema=schema, options=options
        )
        reply = _extract_reply(llm.data)
        if reply is None:
            repaired = await self._provider.generate_structured(
                system=system, user=user + _REPAIR_HINT, output_schema=schema, options=options
            )
            reply = _extract_reply(repaired.data)
            llm = repaired
        if reply is None:
            raise OutputValidationError("Assistant reply was missing after a repair attempt.")

        citations = llm.data.get("citations")
        usage = LLMUsage(
            input_tokens=llm.usage.input_tokens,
            output_tokens=llm.usage.output_tokens,
            model=llm.usage.model,
        )
        return AssistantReply(
            reply=reply,
            citations=list(citations) if citations else None,
            usage=usage,
        )


def _extract_reply(data: dict) -> str | None:
    """Return a non-empty reply string from the model output, or None if absent/blank."""
    reply = data.get("reply")
    if isinstance(reply, str) and reply.strip():
        return reply
    return None
