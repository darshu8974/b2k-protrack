"""Application settings, loaded from environment (AI_ prefix) via pydantic-settings."""

from __future__ import annotations

from functools import lru_cache

from pydantic import AliasChoices, Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Typed configuration for the AI service. Values come from env / .env."""

    model_config = SettingsConfigDict(env_prefix="AI_", env_file=".env", extra="ignore")

    app_name: str = "protrack-ai"
    version: str = "0.1.0"
    environment: str = "local"

    # Service-to-service auth (must match the Spring Boot PROTRACK_AI_INTERNAL_KEY).
    internal_key: str = "dev-internal-key"

    # LLM provider abstraction. Defaults to the deterministic mock so the whole pipeline
    # runs with no external dependency; switch to Claude by setting AI_PROVIDER=claude.
    # Accepts AI_PROVIDER (documented switch) or AI_LLM_PROVIDER (prefixed form).
    llm_provider: str = Field(
        default="mock",
        validation_alias=AliasChoices("AI_PROVIDER", "AI_LLM_PROVIDER"),
    )
    # The Claude key lives ONLY in the AI service env, never in Spring. Accepts the standard
    # ANTHROPIC_API_KEY (also read by the Anthropic SDK) or the prefixed AI_ANTHROPIC_API_KEY.
    anthropic_api_key: str = Field(
        default="",
        validation_alias=AliasChoices("ANTHROPIC_API_KEY", "AI_ANTHROPIC_API_KEY"),
    )
    claude_model: str = "claude-sonnet-4-6"
    llm_max_tokens: int = 4096
    llm_temperature: float = 0.2

    # Progress callbacks back to Spring Boot
    spring_callback_base_url: str = "http://localhost:8080"
    request_timeout_ms: int = 120_000

    @property
    def active_model(self) -> str:
        """The model identifier reported for the active provider."""
        return self.claude_model if self.llm_provider == "claude" else self.llm_provider


@lru_cache
def get_settings() -> Settings:
    """Cached settings accessor (single instance per process)."""
    return Settings()
