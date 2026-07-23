"""Prompt-template tests (Sprint 3): every system/user template renders cleanly and stays
provider-agnostic. These are pure template checks — no network, no provider involved.
"""

from __future__ import annotations

import re
from dataclasses import dataclass

from app.prompts.registry import (
    ASSISTANT_V1,
    MANUSCRIPT_ANALYSIS_V1,
    PREFLIGHT_FINDINGS_V1,
    PromptRegistry,
)
from app.schemas.internal import ProjectContext

# Provider-specific structured-output language (Claude's tool-use) must never appear in a
# rendered prompt — Gemini's JSON mode has no "tool" to call, and instructing it to call one that
# doesn't exist in the request is a correctness bug, not a style choice. "structured" alone is
# fine (provider-neutral); only flag the tool-calling phrasing specifically.
_TOOL_LANGUAGE = re.compile(r"\bcall(?:ing)?\s+the\b.*\btool\b|\bvia the tool\b", re.IGNORECASE)


def _project() -> ProjectContext:
    return ProjectContext(
        project_id="p-1",
        title="Intro to Quantum Mechanics",
        publication_type="STEM_TEXTBOOK",
        discipline="Physics",
        current_stage="AI_ANALYSIS",
    )


@dataclass
class _Counts:
    pages: int = 320
    figures: int = 45
    tables: int = 12
    equations: int = 210
    problems: int = 60
    references: int = 88


@dataclass
class _Heading:
    level: str
    text: str


def _analysis_user_context() -> dict:
    return {
        "project": _project(),
        "counts": _Counts(),
        "headings": [_Heading("H1", "Chapter 1")],
        "language": "en",
        "sample": "This chapter introduces the postulates of quantum mechanics...",
    }


def _preflight_user_context() -> dict:
    return {
        "standard": "PDF/X-1a",
        "findings": [
            {
                "checkKey": "font_embedding",
                "result": "FAIL",
                "category": "fonts",
                "pageRef": "12",
                "evidence": "2 fonts not embedded: Helvetica, Symbol",
            }
        ],
    }


def _assistant_user_context() -> dict:
    return {"project": _project(), "history": [], "message": "What stage is this project in?"}


_PROMPTS = [
    (MANUSCRIPT_ANALYSIS_V1, _analysis_user_context),
    (PREFLIGHT_FINDINGS_V1, _preflight_user_context),
    (ASSISTANT_V1, _assistant_user_context),
]


def test_every_system_and_user_template_renders_without_error() -> None:
    registry = PromptRegistry()
    for prompt_id, build_context in _PROMPTS:
        system = registry.render_system(prompt_id)
        user = registry.render_user(prompt_id, build_context())
        assert system.strip()
        assert user.strip()


def test_no_provider_specific_tool_calling_language_in_any_rendered_prompt() -> None:
    """Regression guard for the Sprint-3 fix: prompts must stay provider-agnostic. Claude's
    tool-use and Gemini's JSON mode are both driven by the request, not by prompt wording — no
    template may instruct the model to "call a tool" that may not exist in the request."""
    registry = PromptRegistry()
    for prompt_id, build_context in _PROMPTS:
        system = registry.render_system(prompt_id)
        user = registry.render_user(prompt_id, build_context())
        assert not _TOOL_LANGUAGE.search(system), f"{prompt_id} system prompt still names a tool"
        assert not _TOOL_LANGUAGE.search(user), f"{prompt_id} user prompt still names a tool"


def test_manuscript_analysis_system_prompt_establishes_the_senior_persona() -> None:
    system = PromptRegistry().render_system(MANUSCRIPT_ANALYSIS_V1)
    assert "Senior Scholarly Publishing Production Analyst" in system


def test_manuscript_analysis_system_prompt_includes_complexity_calibration() -> None:
    system = PromptRegistry().render_system(MANUSCRIPT_ANALYSIS_V1)
    assert "Calibration" in system
    assert "complexityScore" in system


def test_assistant_user_prompt_includes_the_projects_current_stage() -> None:
    """Regression guard: ProjectContext used to omit current_stage entirely, so the assistant
    could never answer its own suggested prompt ("What stage is this project in?") — confirmed
    live, where Gemini correctly replied the stage was "not provided" because it genuinely
    wasn't sent. Fixed by threading current_stage through Spring's ProjectContextInfo and this
    schema, then rendering it here."""
    user = PromptRegistry().render_user(ASSISTANT_V1, _assistant_user_context())
    assert "currentStage: AI_ANALYSIS" in user
