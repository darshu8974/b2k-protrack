-- ============================================================================
-- Protrack — V13 role hierarchy refactor (Sprint 8)
-- ----------------------------------------------------------------------------
-- Replaces the original 4-role model (ADMIN, PM, DESIGNER, QA) with the new
-- 5-role hierarchy: ADMIN, PROJECT_MANAGER, PAGINATOR, QC, QA. Inserts a new
-- QC_REVIEW workflow stage between PDF_REVIEW and QA_SIGNOFF, and swaps the four
-- old demo personas for neutral, role-descriptive seed users (plus a QC user).
--
-- Forward-only: V2/V4 are immutable (already applied). Roles and the four demo
-- users are UPDATED in place (role_id / user UUID stay stable) so every existing
-- FK reference — user_roles, project ownership, history, audit — remains valid.
-- ============================================================================

-- ── Roles: rename in place (role_id preserved) + add QC ─────────────────────
UPDATE roles SET code = 'PROJECT_MANAGER', name = 'Project Manager',
    description = 'Manages projects and assigns users to projects; oversees the pipeline. Cannot create admins.'
    WHERE code = 'PM';
UPDATE roles SET code = 'PAGINATOR', name = 'Paginator',
    description = 'Performs pagination and layout work; updates assigned tasks and uploads production PDFs.'
    WHERE code = 'DESIGNER';
UPDATE roles SET name = 'Administrator',
    description = 'Full access: manages users and roles, creates project managers, assigns roles, deletes users, manages all projects.'
    WHERE code = 'ADMIN';
UPDATE roles SET name = 'QA',
    description = 'Final approval: signs off and completes projects.'
    WHERE code = 'QA';

INSERT INTO roles (code, name, description) VALUES
    ('QC', 'QC', 'Reviews the paginator''s work and approves or rejects it before QA sign-off.');

-- ── Workflow: insert QC_REVIEW at sequence 6 (shift QA_SIGNOFF/COMPLETED up) ──
-- Offset by +100 first to avoid transient uq_workflow_stages_sequence collisions,
-- then settle the shifted rows back down by 99 (6→106→7, 7→107→8).
UPDATE workflow_stages SET sequence = sequence + 100 WHERE sequence >= 6;
INSERT INTO workflow_stages (code, name, sequence, description) VALUES
    ('QC_REVIEW', 'QC Review', 6, 'QC reviews the paginator''s pages and approves or rejects them.');
UPDATE workflow_stages SET sequence = sequence - 99 WHERE sequence >= 106;
UPDATE workflow_stages SET description = 'QA performs final approval and e-signs.' WHERE code = 'QA_SIGNOFF';

-- ── Demo users: swap the four named personas for neutral seed identities ─────
-- Updated in place so any runtime data (owned projects, history, audit) stays linked.
UPDATE users SET email = 'admin@protrack.io', full_name = 'Admin User',
    avatar_initials = 'AU', avatar_color = '#6D5EF0', updated_at = now()
    WHERE email = 'david.cho@protrack.io';
UPDATE users SET email = 'pm@protrack.io', full_name = 'Project Manager',
    avatar_initials = 'PM', avatar_color = '#0B63CE', updated_at = now()
    WHERE email = 'priya.anand@protrack.io';
UPDATE users SET email = 'paginator@protrack.io', full_name = 'Paginator',
    avatar_initials = 'PG', avatar_color = '#1F9D57', updated_at = now()
    WHERE email = 'marcus.reed@protrack.io';
UPDATE users SET email = 'qa@protrack.io', full_name = 'QA Approver',
    avatar_initials = 'QA', avatar_color = '#C9821A', updated_at = now()
    WHERE email = 'lena.ortiz@protrack.io';

-- New QC reviewer (shares the demo password "password"; same BCrypt hash as the others).
INSERT INTO users (id, organization_id, email, password_hash, full_name, avatar_initials, avatar_color)
SELECT gen_random_uuid(), o.id, 'qc@protrack.io',
       '$2b$10$fEa0UTsrJtEDoUWgy7ORHOntDSlbM2YddeSjUC2v/ID5TiOaLIgJG',
       'QC Reviewer', 'QC', '#0891B2'
FROM organizations o
WHERE o.slug = 'protrack';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'QC'
WHERE u.email = 'qc@protrack.io';

-- ── Keep historical role labels consistent with the renamed role codes ──────
UPDATE project_stage_history SET triggered_role = 'PROJECT_MANAGER' WHERE triggered_role = 'PM';
UPDATE project_stage_history SET triggered_role = 'PAGINATOR'       WHERE triggered_role = 'DESIGNER';
UPDATE project_members       SET role_in_project = 'PROJECT_MANAGER' WHERE role_in_project = 'PM';
UPDATE project_members       SET role_in_project = 'PAGINATOR'       WHERE role_in_project = 'DESIGNER';
