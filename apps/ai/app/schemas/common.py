"""Shared enums and type aliases for the AI service contract.

Values conform to the approved DB conventions (confidence 0–100; enums as fixed strings).
"""

from __future__ import annotations

from enum import Enum
from typing import Annotated

from pydantic import Field

# Confidence / score values are integers in [0, 100].
Confidence = Annotated[int, Field(ge=0, le=100)]


class Severity(str, Enum):
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


class CheckResult(str, Enum):
    PASS = "PASS"
    REVIEW = "REVIEW"
    FAIL = "FAIL"


class JobStatus(str, Enum):
    QUEUED = "QUEUED"
    RUNNING = "RUNNING"
    SUCCEEDED = "SUCCEEDED"
    FAILED = "FAILED"
