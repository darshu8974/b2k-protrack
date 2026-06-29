"""Prompt template registry — versioned templates with a stable prompt_id stamped on results.

Templates live in prompts/templates/ (Jinja2) and JSON output schemas in prompts/output_schemas/.
Loading/rendering is implemented in Sprint 4.
"""

from __future__ import annotations

from pathlib import Path

TEMPLATES_DIR = Path(__file__).parent / "templates"
OUTPUT_SCHEMAS_DIR = Path(__file__).parent / "output_schemas"


class PromptRegistry:
    def render(self, prompt_id: str, context: dict) -> str:
        raise NotImplementedError("Prompt rendering is implemented in Sprint 4")

    def output_schema(self, prompt_id: str) -> dict:
        raise NotImplementedError("Output schema loading is implemented in Sprint 4")
