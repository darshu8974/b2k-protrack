"""Fetches input bytes from a signed, short-lived URL provided by Spring Boot.

Phase 1: signed download URL to the api's internal file endpoint. Future: presigned S3 URL.
Implemented in Sprint 4. httpx is imported lazily inside the method.
"""

from __future__ import annotations


class FileLoader:
    def __init__(self, internal_key: str, timeout_ms: int = 120_000) -> None:
        self._internal_key = internal_key
        self._timeout_ms = timeout_ms

    async def fetch(self, file_url: str) -> bytes:
        raise NotImplementedError("File loading is implemented in Sprint 4")
