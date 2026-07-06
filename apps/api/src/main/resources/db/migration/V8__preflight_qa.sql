-- ============================================================================
-- Protrack — V8 preflight & QA
-- ----------------------------------------------------------------------------
-- The post-InDesign half of the pipeline (Sprint 5) per the approved Database
-- Design:
--   preflight_runs · preflight_checks · qa_issues · qa_issue_decisions
--   · approvals · qa_signoffs
--
-- A designer's production PDF is preflighted by the AI service (one preflight_run
-- per ai_job, with normalized preflight_checks and qa_issues); QA triages each
-- issue (append-only qa_issue_decisions), records gate outcomes (approvals), and
-- completes the title with a formal e-signature (qa_signoffs). The decision /
-- approval / sign-off histories are append-only — the trail IS the record.
--
-- Conventions (approved, matching V4–V7): UUID PK for exposed entities
-- (app-generated v7); enums as VARCHAR + CHECK; score values on a single 0–100
-- scale; owned children (preflight_checks, qa_issues) CASCADE from preflight_runs;
-- business references use the default RESTRICT (no ON DELETE clause); append-only
-- histories carry created_at (+ actor) only and are never updated or deleted;
-- actor columns held as bare UUID FKs to users (RESTRICT), matching prior modules.
-- The preflight_runs.status vocabulary mirrors the existing ai_jobs / JobStatus
-- terminal states (SUCCEEDED, not COMPLETED) for cross-table consistency.
-- No entities/services/APIs/AI service in this task — schema only.
-- ============================================================================

-- ── Preflight runs: one AI PDF-preflight execution against a PDF version ──
CREATE TABLE preflight_runs (
    id             UUID PRIMARY KEY,
    ai_job_id      UUID         NOT NULL,
    project_id     UUID         NOT NULL,
    pdf_version_id UUID         NOT NULL,
    standard       VARCHAR(40),
    overall_score  INT,
    passed         BOOLEAN,
    total_issues   INT          NOT NULL DEFAULT 0,
    high_severity  INT          NOT NULL DEFAULT 0,
    status         VARCHAR(20)  NOT NULL DEFAULT 'RUNNING',
    ran_at         TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_preflight_runs_job FOREIGN KEY (ai_job_id) REFERENCES ai_jobs (id),
    CONSTRAINT fk_preflight_runs_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_preflight_runs_pdf_version FOREIGN KEY (pdf_version_id) REFERENCES file_versions (id),
    CONSTRAINT ck_preflight_runs_score CHECK
        (overall_score IS NULL OR (overall_score >= 0 AND overall_score <= 100)),
    CONSTRAINT ck_preflight_runs_total_issues CHECK (total_issues >= 0),
    CONSTRAINT ck_preflight_runs_high_severity CHECK (high_severity >= 0),
    CONSTRAINT ck_preflight_runs_status CHECK (status IN ('RUNNING', 'SUCCEEDED', 'FAILED'))
);
CREATE INDEX ix_preflight_runs_project ON preflight_runs (project_id, created_at DESC);
CREATE INDEX ix_preflight_runs_job ON preflight_runs (ai_job_id);

-- ── Preflight checks: the live checklist items (geometry, fonts, …) per run ──
CREATE TABLE preflight_checks (
    id                UUID PRIMARY KEY,
    preflight_run_id  UUID         NOT NULL,
    check_key         VARCHAR(60)  NOT NULL,
    result            VARCHAR(10)  NOT NULL,
    detail            TEXT,
    sort_order        INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_preflight_checks_run FOREIGN KEY (preflight_run_id)
        REFERENCES preflight_runs (id) ON DELETE CASCADE,
    CONSTRAINT ck_preflight_checks_result CHECK (result IN ('PASS', 'REVIEW', 'FAIL'))
);
CREATE INDEX ix_preflight_checks_run ON preflight_checks (preflight_run_id);

-- ── QA issues: individual issues raised by a preflight run (the QA work surface) ──
CREATE TABLE qa_issues (
    id               UUID PRIMARY KEY,
    preflight_run_id UUID         NOT NULL,
    project_id       UUID         NOT NULL,
    category         VARCHAR(60),
    severity         VARCHAR(10)  NOT NULL,
    title            VARCHAR(250) NOT NULL,
    recommendation   TEXT,
    page_ref         VARCHAR(40),
    source           VARCHAR(20)  NOT NULL DEFAULT 'AI',
    status           VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_qa_issues_run FOREIGN KEY (preflight_run_id)
        REFERENCES preflight_runs (id) ON DELETE CASCADE,
    CONSTRAINT fk_qa_issues_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT ck_qa_issues_severity CHECK (severity IN ('HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT ck_qa_issues_status CHECK (status IN ('OPEN', 'TRIAGED', 'RESOLVED', 'WAIVED'))
);
CREATE INDEX ix_qa_issues_run ON qa_issues (preflight_run_id);
CREATE INDEX ix_qa_issues_project_status ON qa_issues (project_id, status);

-- ── QA issue decisions: append-only triage trail per issue ──
CREATE TABLE qa_issue_decisions (
    id          UUID PRIMARY KEY,
    issue_id    UUID         NOT NULL,
    decided_by  UUID,
    decision    VARCHAR(20)  NOT NULL,
    comment     TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_qa_issue_decisions_issue FOREIGN KEY (issue_id) REFERENCES qa_issues (id),
    CONSTRAINT fk_qa_issue_decisions_decided_by FOREIGN KEY (decided_by) REFERENCES users (id),
    CONSTRAINT ck_qa_issue_decisions_decision CHECK
        (decision IN ('ACCEPT_FIX', 'SEND_BACK', 'COMMENT'))
);
CREATE INDEX ix_qa_issue_decisions_issue ON qa_issue_decisions (issue_id);

-- ── Approvals: append-only human gate decisions across the workflow ──
CREATE TABLE approvals (
    id            UUID PRIMARY KEY,
    project_id    UUID         NOT NULL,
    stage_code    VARCHAR(40),
    approval_type VARCHAR(60),
    decision      VARCHAR(20)  NOT NULL,
    decided_role  VARCHAR(40),
    decided_by    UUID,
    comment       TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_approvals_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_approvals_decided_by FOREIGN KEY (decided_by) REFERENCES users (id),
    CONSTRAINT ck_approvals_decision CHECK (decision IN ('APPROVED', 'REJECTED'))
);
CREATE INDEX ix_approvals_project ON approvals (project_id, created_at DESC);

-- ── QA sign-offs: the formal QA e-signature attestation (append-only) ──
CREATE TABLE qa_signoffs (
    id                UUID PRIMARY KEY,
    project_id        UUID         NOT NULL,
    preflight_run_id  UUID         NOT NULL,
    signed_by         UUID,
    decision          VARCHAR(20)  NOT NULL,
    quality_score     INT,
    signature_hash    VARCHAR(128),
    artifact_checksum VARCHAR(64),
    notes             TEXT,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_qa_signoffs_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_qa_signoffs_run FOREIGN KEY (preflight_run_id) REFERENCES preflight_runs (id),
    CONSTRAINT fk_qa_signoffs_signed_by FOREIGN KEY (signed_by) REFERENCES users (id),
    CONSTRAINT ck_qa_signoffs_decision CHECK (decision IN ('APPROVED', 'REJECTED')),
    CONSTRAINT ck_qa_signoffs_quality_score CHECK
        (quality_score IS NULL OR (quality_score >= 0 AND quality_score <= 100))
);
CREATE INDEX ix_qa_signoffs_project ON qa_signoffs (project_id, created_at DESC);
CREATE INDEX ix_qa_signoffs_run ON qa_signoffs (preflight_run_id);

-- ============================================================================
-- No seed data: preflight runs, checks, issues, decisions, approvals and
-- sign-offs are all created at runtime by the preflight/QA pipeline (Sprint 5
-- tasks). Consistent with V5/V6/V7.
-- ============================================================================
