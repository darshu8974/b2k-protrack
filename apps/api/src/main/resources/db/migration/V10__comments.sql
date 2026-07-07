-- ============================================================================
-- Protrack — V10 comments
-- ----------------------------------------------------------------------------
-- The Collaboration surface (Sprint 6) per the approved Database Design:
--   comments
--
-- Threaded, project-scoped discussion attached to a polymorphic context (the
-- project itself, a QA issue, a file, …). Self-referencing parent_comment_id
-- gives replies; soft-delete (deleted_at) preserves thread history. Owned by the
-- project module (the Database Design's module-ownership map has no separate
-- "comment" module; the roadmap places comments "in files/project").
--
-- Conventions (approved, matching V4–V9): UUID PK for exposed entities
-- (app-generated v7); hard FKs to project/author/parent held with the default
-- RESTRICT (no ON DELETE clause); context_type kept an UNCONSTRAINED VARCHAR
-- (polymorphic / future-proof, validated at the service layer — consistent with
-- the data-driven doc_type / item_type decisions in V6); mutable table carries
-- created_at + updated_at, plus deleted_at for soft-delete. Indexes on the real
-- access paths (project feed + polymorphic context lookup). No seed data —
-- comments are authored at runtime (consistent with V5/V6/V7/V8/V9).
-- ============================================================================

CREATE TABLE comments (
    id                UUID PRIMARY KEY,
    project_id        UUID         NOT NULL,
    parent_comment_id UUID,
    author_id         UUID         NOT NULL,
    context_type      VARCHAR(40)  NOT NULL DEFAULT 'PROJECT',
    context_id        UUID,
    body              TEXT         NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    CONSTRAINT fk_comments_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments (id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
);
-- The project's comment feed, chronological (DB Design §6).
CREATE INDEX ix_comments_project ON comments (project_id, created_at);
-- Polymorphic context lookup (comments on a specific issue / file / …).
CREATE INDEX ix_comments_context ON comments (context_type, context_id);

-- ============================================================================
-- No seed data: comments are authored at runtime via the Comments tab.
-- Consistent with V5/V6/V7/V8/V9.
-- ============================================================================
