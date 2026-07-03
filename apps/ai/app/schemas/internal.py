"""Internal payloads shared across endpoints: project context, usage, progress, errors."""

from __future__ import annotations

from pydantic import Field

from app.schemas.common import CamelModel, JobStatus


class ProjectContext(CamelModel):
    """Minimal project context passed by Spring Boot for scoping AI work."""

    project_id: str
    title: str | None = None
    publication_type: str | None = None
    discipline: str | None = None


class LLMUsage(CamelModel):
    """Normalized token-usage accounting returned by a provider."""

    input_tokens: int = 0
    output_tokens: int = 0
    model: str | None = None


class ProgressCallback(CamelModel):
    """Posted by the AI service to Spring Boot during a long job."""

    job_id: str
    progress_pct: int = Field(ge=0, le=100)
    status: JobStatus
    partial: dict | None = None


class ErrorPayload(CamelModel):
    """Structured error body returned on failure."""

    code: str
    message: str
    retryable: bool = False
