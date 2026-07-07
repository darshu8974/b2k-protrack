"""Prompt template registry — versioned templates with a stable prompt_id stamped on results.

Templates live in ``prompts/templates/`` (Jinja2), split into ``<prompt_id>.system.jinja`` and
``<prompt_id>.user.jinja``; JSON output schemas in ``prompts/output_schemas/<prompt_id>.json`` are
the single source of truth shared by the provider's structured-output tool and the response models.
Templates and schemas are cached after first load.
"""

from __future__ import annotations

import json
from functools import lru_cache
from pathlib import Path
from typing import Any

from jinja2 import Environment, FileSystemLoader, StrictUndefined, select_autoescape

TEMPLATES_DIR = Path(__file__).parent / "templates"
OUTPUT_SCHEMAS_DIR = Path(__file__).parent / "output_schemas"

MANUSCRIPT_ANALYSIS_V1 = "manuscript_analysis.v1"
PREFLIGHT_FINDINGS_V1 = "preflight_findings.v1"
ASSISTANT_V1 = "assistant.v1"


class PromptRegistry:
    def __init__(self) -> None:
        self._env = Environment(
            loader=FileSystemLoader(str(TEMPLATES_DIR)),
            autoescape=select_autoescape(enabled_extensions=(), default=False),
            undefined=StrictUndefined,
            trim_blocks=False,
            keep_trailing_newline=True,
        )

    def render_system(self, prompt_id: str, context: dict[str, Any] | None = None) -> str:
        return self._env.get_template(f"{prompt_id}.system.jinja").render(**(context or {}))

    def render_user(self, prompt_id: str, context: dict[str, Any]) -> str:
        return self._env.get_template(f"{prompt_id}.user.jinja").render(**context)

    def output_schema(self, prompt_id: str) -> dict[str, Any]:
        return _load_schema(prompt_id)


@lru_cache
def _load_schema(prompt_id: str) -> dict[str, Any]:
    path = OUTPUT_SCHEMAS_DIR / f"{prompt_id}.json"
    return json.loads(path.read_text(encoding="utf-8"))
