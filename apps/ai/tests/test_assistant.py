"""Assistant chat route + orchestrator + provider-branch tests (hermetic, mock provider)."""

from __future__ import annotations

from fastapi.testclient import TestClient

from app.main import app
from app.orchestration.assistant_orchestrator import AssistantOrchestrator
from app.prompts.registry import ASSISTANT_V1, PromptRegistry
from app.providers.base import LLMResponse, Usage
from app.providers.mock_provider import MockProvider
from app.schemas.assistant import AssistantChatRequest, AssistantMessage

_HEADERS = {"X-Internal-Key": "dev-internal-key"}


def test_assistant_chat_end_to_end() -> None:
    client = TestClient(app)
    response = client.post(
        "/internal/v1/assistant/chat",
        headers=_HEADERS,
        json={
            "message": "What stage is this project in?",
            "projectContext": {"projectId": "p-1", "title": "Quantum Mechanics 3e"},
            "history": [{"role": "user", "content": "Hi"}],
        },
    )
    assert response.status_code == 200, response.text
    body = response.json()
    # camelCase wire contract (matches the API spec + Spring's Jackson).
    assert body["reply"]
    assert body["usage"]["model"] == "mock"
    assert isinstance(body.get("citations"), list)


def test_assistant_chat_requires_internal_key() -> None:
    client = TestClient(app)
    response = client.post("/internal/v1/assistant/chat", json={"message": "hello"})
    assert response.status_code == 401


async def test_mock_provider_returns_assistant_payload_for_reply_schema() -> None:
    """The mock selects its assistant payload from the schema's top-level ``reply`` property."""
    schema = PromptRegistry().output_schema(ASSISTANT_V1)
    response = await MockProvider().generate_structured(system="s", user="u", output_schema=schema)

    assert "reply" in response.data
    assert "issues" not in response.data  # not the preflight branch
    assert "overallConfidence" not in response.data  # not the analysis branch


async def test_orchestrator_builds_reply_with_history_and_context() -> None:
    orchestrator = AssistantOrchestrator(provider=MockProvider(), prompt_registry=PromptRegistry())
    request = AssistantChatRequest(
        message="Summarize progress",
        history=[AssistantMessage(role="user", content="earlier question")],
    )
    reply = await orchestrator.run(request)
    assert reply.reply
    assert reply.usage is not None


async def test_orchestrator_repairs_when_reply_missing() -> None:
    """A first blank reply triggers exactly one repair re-prompt, then succeeds."""

    class FlakyProvider:
        name = "flaky"

        def __init__(self) -> None:
            self.calls = 0

        async def generate_structured(self, **_: object) -> LLMResponse:
            self.calls += 1
            data = {} if self.calls == 1 else {"reply": "Recovered answer."}
            return LLMResponse(data=data, usage=Usage(model="flaky"))

    provider = FlakyProvider()
    orchestrator = AssistantOrchestrator(provider=provider, prompt_registry=PromptRegistry())
    reply = await orchestrator.run(AssistantChatRequest(message="anything"))
    assert reply.reply == "Recovered answer."
    assert provider.calls == 2
