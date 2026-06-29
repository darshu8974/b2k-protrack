"""Protrack AI Service — FastAPI application entry point.

Stateless service: parses documents, calls the LLM, validates/normalizes, and returns results.
This Sprint-0 scaffold wires configuration, logging, observability, error handling, security,
and route skeletons. AI business logic is added in Sprints 4–6.
"""

from __future__ import annotations

from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

import structlog
from fastapi import FastAPI, Request, Response

from app.api.routes import analyze, assistant, health, preflight
from app.core.config import get_settings
from app.core.errors import register_exception_handlers
from app.core.logging import configure_logging
from app.core.observability import setup_observability

CORRELATION_HEADER = "X-Correlation-Id"


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    configure_logging()
    yield


def create_app() -> FastAPI:
    settings = get_settings()
    app = FastAPI(
        title="Protrack AI Service",
        version=settings.version,
        lifespan=lifespan,
        docs_url="/internal/v1/docs",
        openapi_url="/internal/v1/openapi.json",
    )

    @app.middleware("http")
    async def bind_trace_id(request: Request, call_next) -> Response:  # type: ignore[no-untyped-def]
        trace_id = request.headers.get(CORRELATION_HEADER)
        structlog.contextvars.clear_contextvars()
        if trace_id:
            structlog.contextvars.bind_contextvars(trace_id=trace_id)
        response = await call_next(request)
        if trace_id:
            response.headers[CORRELATION_HEADER] = trace_id
        return response

    register_exception_handlers(app)
    setup_observability(app)

    app.include_router(health.router)
    app.include_router(analyze.router)
    app.include_router(preflight.router)
    app.include_router(assistant.router)
    return app


app = create_app()
