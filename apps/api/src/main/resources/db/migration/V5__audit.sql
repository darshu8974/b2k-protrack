-- ============================================================================
-- Protrack — V5 audit trail
-- ----------------------------------------------------------------------------
-- Append-only system of record for business events (project lifecycle, workflow
-- transitions, user actions). Immutability is enforced at the application layer
-- (no UPDATE/DELETE code path); metadata is stored as JSONB for flexible detail.
-- ============================================================================

CREATE TABLE audit_events (
    id              UUID PRIMARY KEY,
    organization_id UUID,
    project_id      UUID,
    actor_id        UUID,
    actor_type      VARCHAR(20)  NOT NULL DEFAULT 'USER',
    event_type      VARCHAR(60)  NOT NULL,
    entity_type     VARCHAR(40)  NOT NULL,
    entity_id       UUID,
    summary         VARCHAR(400) NOT NULL,
    metadata        JSONB,
    correlation_id  VARCHAR(64),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_audit_events_project ON audit_events (project_id, created_at DESC);
CREATE INDEX ix_audit_events_org ON audit_events (organization_id, created_at DESC);
CREATE INDEX ix_audit_events_entity ON audit_events (entity_type, entity_id);
