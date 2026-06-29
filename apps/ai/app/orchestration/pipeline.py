"""Linear pipeline runner skeleton.

Orchestrators compose ordered steps (load -> parse -> build_prompt -> call_llm -> validate ->
normalize), emitting progress between steps. The concrete runner is implemented in Sprint 4.
"""

from __future__ import annotations

from collections.abc import Awaitable, Callable
from typing import Any

Step = Callable[[dict[str, Any]], Awaitable[dict[str, Any]]]


class Pipeline:
    def __init__(self, steps: list[tuple[str, Step]] | None = None) -> None:
        self._steps = steps or []

    async def run(self, initial: dict[str, Any]) -> dict[str, Any]:
        raise NotImplementedError("Pipeline execution is implemented in Sprint 4")
