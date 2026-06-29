"""Internal payloads shared across endpoints: project context, usage, progress, errors."""

from __future__ import annotations

from pydantic import BaseModel, Field

from app.schemas.common import JobStatus


class ProjectContext(BaseModel):
    """Minimal project context passed by Spring Boot for scoping AI work."""

    project_id: str
    title: str | None = None
    publication_type: str | None = None
    discipline: str | None = None


class LLMUsage(BaseModel):
    """Normalized token-usage accounting returned by a provider."""

    input_tokens: int = 0
    output_tokens: int = 0
    model: str | None = None


class ProgressCallback(BaseModel):
    """Posted by the AI service to Spring Boot during a long job."""

    job_id: str
    progress_pct: int = Field(ge=0, le=100)
    status: JobStatus
    partial: dict | None = None


class ErrorPayload(BaseModel):
    """Structured error body returned on failure."""

    code: str
    message: str
    retryable: bool = False
