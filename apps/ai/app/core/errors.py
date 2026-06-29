"""AI service error taxonomy and FastAPI exception handlers.

Errors map to a structured ErrorPayload body. The taxonomy classifies failures as
retryable (transient) or not, which the retry policy and the Spring Boot caller rely on.
"""

from __future__ import annotations

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.schemas.internal import ErrorPayload


class AiServiceError(Exception):
    """Base error. Subclasses set status_code / code / retryable."""

    code: str = "AI_ERROR"
    status_code: int = 500
    retryable: bool = False

    def __init__(self, message: str) -> None:
        super().__init__(message)
        self.message = message


class TransientProviderError(AiServiceError):
    """Provider/network failure that should be retried (429/5xx/timeout)."""

    code = "PROVIDER_TRANSIENT"
    status_code = 503
    retryable = True


class OutputValidationError(AiServiceError):
    """LLM output did not match the expected schema (bounded repair retry)."""

    code = "OUTPUT_VALIDATION"
    status_code = 502
    retryable = True


class PermanentError(AiServiceError):
    """Unrecoverable input/processing error — fail fast."""

    code = "PERMANENT"
    status_code = 400
    retryable = False


class DownstreamError(AiServiceError):
    """Failure fetching an input artifact (e.g. file download)."""

    code = "DOWNSTREAM"
    status_code = 502
    retryable = True


def register_exception_handlers(app: FastAPI) -> None:
    """Register handlers that render AiServiceError as ErrorPayload JSON."""

    @app.exception_handler(AiServiceError)
    async def _handle_ai_error(_: Request, exc: AiServiceError) -> JSONResponse:
        payload = ErrorPayload(code=exc.code, message=exc.message, retryable=exc.retryable)
        return JSONResponse(status_code=exc.status_code, content=payload.model_dump())
