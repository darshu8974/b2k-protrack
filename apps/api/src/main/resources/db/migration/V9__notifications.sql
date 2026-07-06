-- ============================================================================
-- Protrack — V9 notifications
-- ----------------------------------------------------------------------------
-- The Platform-Services notification surface (Sprint 6) per the approved
-- Database Design:
--   notifications · notification_preferences
--
-- Cross-cutting subscribers (the notification module's NotificationEventListener)
-- fan out per-recipient rows from domain events already published by the
-- project/workflow/ai/qa modules. Each row is an in-app feed item (the bell);
-- when the recipient's per-type email preference is enabled the same event is
-- also delivered by email (best-effort) and stamped via sent_at. Read state
-- (is_read / read_at) is the only mutation — notifications otherwise carry the
-- single created_at audit column (append-only style, matching V5/V8 histories).
--
-- Conventions (approved, matching V4–V8): UUID PK for exposed entities
-- (app-generated v7); enums as VARCHAR + CHECK; actor/reference columns held as
-- bare UUID FKs (RESTRICT, no ON DELETE clause) matching prior modules; indexes
-- on the real access paths (recipient feed + unread badge). No seed data —
-- notifications and preferences are created at runtime (consistent with
-- V5/V6/V7/V8).
-- ============================================================================

-- ── Notifications: per-recipient in-app feed items (+ optional email delivery) ──
CREATE TABLE notifications (
    id                  UUID PRIMARY KEY,
    recipient_id        UUID         NOT NULL,
    related_project_id  UUID,
    type                VARCHAR(60)  NOT NULL,
    title               VARCHAR(250) NOT NULL,
    body                TEXT,
    channel             VARCHAR(20)  NOT NULL DEFAULT 'IN_APP',
    related_entity_type VARCHAR(40),
    related_entity_id   UUID,
    is_read             BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMPTZ,
    sent_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id),
    CONSTRAINT fk_notifications_project FOREIGN KEY (related_project_id) REFERENCES projects (id),
    CONSTRAINT ck_notifications_channel CHECK (channel IN ('IN_APP', 'EMAIL'))
);
-- Unread-badge count + the recipient's feed, newest first (DB Design §6).
CREATE INDEX ix_notifications_recipient_read ON notifications (recipient_id, is_read);
CREATE INDEX ix_notifications_recipient_created ON notifications (recipient_id, created_at DESC);

-- ── Notification preferences: per-user, per-type channel opt-in ──
-- Absence of a row means both channels are enabled (default-on). Phase-1 minimal:
-- no timestamp columns (per ERD) — the row is a small, directly-mutated setting.
CREATE TABLE notification_preferences (
    id             UUID PRIMARY KEY,
    user_id        UUID         NOT NULL,
    type           VARCHAR(60)  NOT NULL,
    in_app_enabled BOOLEAN      NOT NULL DEFAULT TRUE,
    email_enabled  BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_notification_preferences_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_notification_preferences_user_type UNIQUE (user_id, type)
);
CREATE INDEX ix_notification_preferences_user ON notification_preferences (user_id);

-- ============================================================================
-- No seed data: notifications are produced at runtime by the notification
-- event listener; preference rows are created lazily when a user changes a
-- default. Consistent with V5/V6/V7/V8.
-- ============================================================================
