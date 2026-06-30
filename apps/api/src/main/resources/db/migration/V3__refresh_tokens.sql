-- ============================================================================
-- Protrack — V3 refresh tokens
-- ----------------------------------------------------------------------------
-- Stores refresh tokens for the auth flow. Only a SHA-256 hash of each token is
-- persisted (never the raw value); the raw token is returned to the client once.
-- Supports rotation (replaced_by) and revocation (revoked_at).
-- ============================================================================

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL,
    token_hash  VARCHAR(64)  NOT NULL,   -- SHA-256 hex of the raw token
    issued_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked_at  TIMESTAMPTZ,
    replaced_by UUID,                     -- the token that rotated this one out
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX ix_refresh_tokens_user ON refresh_tokens (user_id);
