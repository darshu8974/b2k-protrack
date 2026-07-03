"""Shared enums and type aliases for the AI service contract.

Values conform to the approved DB conventions (confidence 0–100; enums as fixed strings).
"""

from __future__ import annotations

from enum import StrEnum
from typing import Annotated

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel


class CamelModel(BaseModel):
    """Base model whose JSON wire form is camelCase (the API contract), while Python stays
    snake_case. Accepts both spellings on input (populate_by_name)."""

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


# Confidence / score values are integers in [0, 100].
Confidence = Annotated[int, Field(ge=0, le=100)]


class Severity(StrEnum):
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


class CheckResult(StrEnum):
    PASS = "PASS"
    REVIEW = "REVIEW"
    FAIL = "FAIL"


class JobStatus(StrEnum):
    QUEUED = "QUEUED"
    RUNNING = "RUNNING"
    SUCCEEDED = "SUCCEEDED"
    FAILED = "FAILED"
