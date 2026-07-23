"""Sprint 1 connectivity smoke test: proves the configured Gemini API key can actually reach
Gemini from this FastAPI service. This is deliberately NOT part of the provider abstraction
(that's the real `GeminiProvider`, covered in test_providers.py) — it only verifies configuration
+ connectivity using the raw SDK.

Opt-in only, via AI_RUN_LIVE_SMOKE_TEST — NOT merely "GEMINI_API_KEY is set". Once a developer
follows Sprint 1/4's own instructions and puts a real key in apps/ai/.env for normal app use, that
key stays there indefinitely; gating on its mere presence would make every future plain `pytest`
run silently fire a real, billed network call forever. Requiring an explicit opt-in keeps the
default test suite hermetic (see conftest.py's `_hermetic_provider_settings`) while still letting
you verify connectivity on demand:

    AI_RUN_LIVE_SMOKE_TEST=1 pytest tests/test_gemini_smoke.py -v -s
"""

from __future__ import annotations

import os

import pytest

from app.core.config import get_settings

settings = get_settings()

_opted_in = os.environ.get("AI_RUN_LIVE_SMOKE_TEST", "").lower() in {"1", "true", "yes"}

pytestmark = pytest.mark.skipif(
    not _opted_in,
    reason=(
        "Opt-in only (fires a real, billed Gemini call) — set AI_RUN_LIVE_SMOKE_TEST=1 to run."
    ),
)


async def test_gemini_api_key_can_reach_gemini() -> None:
    assert settings.gemini_api_key, "GEMINI_API_KEY must be set in apps/ai/.env to run this test."

    from google import genai

    client = genai.Client(api_key=settings.gemini_api_key)
    response = await client.aio.models.generate_content(
        model=settings.gemini_model,
        contents="Reply with exactly one word: OK",
    )

    assert response.text is not None
    assert "OK" in response.text.upper()
    print(f"\nGemini smoke test OK — model={settings.gemini_model} reply={response.text!r}")
