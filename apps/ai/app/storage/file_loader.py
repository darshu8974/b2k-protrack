"""Fetches input bytes from a short-lived signed URL provided by Spring Boot.

The AI service is stateless and never touches the database or storage directly — it is handed a
fetchable URL (from ``FilesFacade.signedDownloadUrl``). Phase 1 with the local storage driver
yields a ``file://`` URI (same host); ``http(s)://`` covers the API's download endpoint and future
S3 presigned URLs. Nothing is persisted: bytes are returned in memory.
"""

from __future__ import annotations

from pathlib import Path
from urllib.parse import urlparse
from urllib.request import url2pathname

from app.core.errors import DownstreamError, PermanentError

_MAX_BYTES = 100 * 1024 * 1024  # 100 MB safety cap


class FileLoader:
    def __init__(self, internal_key: str, timeout_ms: int = 120_000) -> None:
        self._internal_key = internal_key
        self._timeout_s = timeout_ms / 1000

    async def fetch(self, file_url: str) -> bytes:
        scheme = urlparse(file_url).scheme.lower()
        if scheme == "file":
            return self._fetch_file(file_url)
        if scheme in ("http", "https"):
            return await self._fetch_http(file_url)
        raise PermanentError(f"Unsupported file URL scheme: {scheme or '(none)'}")

    def _fetch_file(self, file_url: str) -> bytes:
        path = Path(url2pathname(urlparse(file_url).path))
        if not path.is_file():
            raise DownstreamError(f"Signed file not found: {path}")
        size = path.stat().st_size
        if size > _MAX_BYTES:
            raise PermanentError(f"File exceeds the maximum size of {_MAX_BYTES} bytes.")
        return path.read_bytes()

    async def _fetch_http(self, file_url: str) -> bytes:
        import httpx

        try:
            async with httpx.AsyncClient(timeout=self._timeout_s) as client:
                response = await client.get(
                    file_url, headers={"X-Internal-Key": self._internal_key}
                )
                response.raise_for_status()
        except httpx.HTTPError as exc:
            raise DownstreamError(f"Failed to fetch file: {exc}") from exc
        content = response.content
        if len(content) > _MAX_BYTES:
            raise PermanentError(f"File exceeds the maximum size of {_MAX_BYTES} bytes.")
        return content
