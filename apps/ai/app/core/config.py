"""Application settings, loaded from environment (AI_ prefix) via pydantic-settings."""

from __future__ import annotations

from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Typed configuration for the AI service. Values come from env / .env."""

    model_config = SettingsConfigDict(env_prefix="AI_", env_file=".env", extra="ignore")

    app_name: str = "protrack-ai"
    version: str = "0.1.0"
    environment: str = "local"

    # Service-to-service auth (must match the Spring Boot PROTRACK_AI_INTERNAL_KEY).
    internal_key: str = "dev-internal-key"

    # LLM provider abstraction
    llm_provider: str = "claude"
    anthropic_api_key: str = ""
    claude_model: str = "claude-sonnet-4-6"
    llm_max_tokens: int = 4096
    llm_temperature: float = 0.2

    # Progress callbacks back to Spring Boot
    spring_callback_base_url: str = "http://localhost:8080"
    request_timeout_ms: int = 120_000


@lru_cache
def get_settings() -> Settings:
    """Cached settings accessor (single instance per process)."""
    return Settings()
