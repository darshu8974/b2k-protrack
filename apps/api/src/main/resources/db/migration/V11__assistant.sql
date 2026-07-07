-- ============================================================================
-- Protrack — V11 assistant
-- ----------------------------------------------------------------------------
-- The scoped AI Assistant surface (Sprint 6) per the approved Database Design:
--   assistant_threads
--   assistant_messages
--
-- A per-(project, user) conversation thread holding an ordered list of messages
-- (role = USER | ASSISTANT). The assistant answers project-scoped questions via
-- the FastAPI AI service (Claude). Messages optionally link to the originating
-- ai_job (nullable — a chat turn is synchronous and does not create an AiJob).
--
-- Conventions (approved, matching V4–V10): UUID PK for exposed entities
-- (app-generated); hard FKs to project/user held with the default RESTRICT (no
-- ON DELETE clause), EXCEPT the owned-child link assistant_messages -> assistant_threads
-- which CASCADEs (owned children cascade from their parent — DB Design §5/§6,
-- consistent with analysis_*/preflight_checks); role kept an UNCONSTRAINED VARCHAR
-- validated at the service layer (data-driven, consistent with prior enums-as-strings
-- where the set is small and app-owned); mutable-free append-only tables carry only
-- created_at. Indexes on the real access paths (a user's thread for a project;
-- a thread's messages in order). No seed data — threads/messages are created at
-- runtime (consistent with V5–V10).
-- ============================================================================

CREATE TABLE assistant_threads (
    id          UUID PRIMARY KEY,
    project_id  UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_assistant_threads_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_assistant_threads_user FOREIGN KEY (user_id) REFERENCES users (id),
    -- One assistant thread per (project, user): the GET .../assistant/thread endpoint is singular.
    CONSTRAINT uq_assistant_threads_project_user UNIQUE (project_id, user_id)
);

CREATE TABLE assistant_messages (
    id         UUID PRIMARY KEY,
    thread_id  UUID         NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    content    TEXT         NOT NULL,
    tokens     INT,
    ai_job_id  UUID,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_assistant_messages_thread FOREIGN KEY (thread_id)
        REFERENCES assistant_threads (id) ON DELETE CASCADE,
    CONSTRAINT fk_assistant_messages_job FOREIGN KEY (ai_job_id) REFERENCES ai_jobs (id),
    CONSTRAINT ck_assistant_messages_tokens CHECK (tokens IS NULL OR tokens >= 0)
);

-- A thread's messages in chronological order (the chat transcript).
CREATE INDEX ix_assistant_messages_thread ON assistant_messages (thread_id, created_at);

-- ============================================================================
-- No seed data: threads and messages are authored at runtime via the Assistant tab.
-- Consistent with V5/V6/V7/V8/V9/V10.
-- ============================================================================
