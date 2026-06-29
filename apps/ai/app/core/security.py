"""Service-to-service authentication: verifies the shared internal key.

Only the Spring Boot `ai` module should call the business endpoints. The `X-Internal-Key`
header is checked in constant time. (An HMAC + timestamp anti-replay upgrade is a future
enhancement per the AI Service Architecture.)
"""

from __future__ import annotations

import hmac

from fastapi import Depends, Header, HTTPException, status

from app.core.config import Settings, get_settings


async def verify_internal_key(
    x_internal_key: str | None = Header(default=None),
    settings: Settings = Depends(get_settings),
) -> None:
    """FastAPI dependency that rejects requests without a valid internal key."""
    if not x_internal_key or not hmac.compare_digest(x_internal_key, settings.internal_key):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing internal key",
        )
