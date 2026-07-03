-- ============================================================================
-- Protrack — V7 AI manuscript analysis
-- ----------------------------------------------------------------------------
-- The AI intelligence backbone for Sprint 4 per the approved Database Design:
--   ai_jobs · analysis_results · analysis_metrics · analysis_composition
--   · analysis_headings · analysis_risks · team_suggestions
--
-- ai_jobs records every async AI execution (decoupling the monolith from the
-- FastAPI service); analysis_results is the header of a manuscript analysis with
-- the full model output kept in raw_payload (JSONB) for provenance; the
-- analysis_* children are the normalized rows behind the AI Analysis screen.
--
-- Conventions (approved, matching V4–V6): UUID PK for exposed entities
-- (app-generated v7); enums as VARCHAR + CHECK; confidence/score values on a
-- single 0–100 scale (resolves the Database Design open item, per the API spec
-- and AI Service architecture); owned children CASCADE from their parent;
-- created_by held as a bare UUID (no FK), matching projects/documents.
-- No entities/services/APIs/AI service in this task — schema only.
-- ============================================================================

-- ── AI jobs: one row per async AI execution (analysis / preflight / assistant) ──
CREATE TABLE ai_jobs (
    id               UUID PRIMARY KEY,
    project_id       UUID         NOT NULL,
    job_type         VARCHAR(40)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'QUEUED',
    progress_pct     INT          NOT NULL DEFAULT 0,
    provider         VARCHAR(60),
    model            VARCHAR(120),
    input_version_id UUID,
    started_at       TIMESTAMPTZ,
    finished_at      TIMESTAMPTZ,
    duration_ms      INT,
    error_message    TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       UUID,
    CONSTRAINT fk_ai_jobs_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_ai_jobs_input_version FOREIGN KEY (input_version_id) REFERENCES file_versions (id),
    CONSTRAINT ck_ai_jobs_job_type CHECK (job_type IN
        ('MANUSCRIPT_ANALYSIS', 'PDF_PREFLIGHT', 'ASSISTANT')),
    CONSTRAINT ck_ai_jobs_status CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCEEDED', 'FAILED')),
    CONSTRAINT ck_ai_jobs_progress CHECK (progress_pct BETWEEN 0 AND 100),
    CONSTRAINT ck_ai_jobs_duration CHECK (duration_ms IS NULL OR duration_ms >= 0)
);
CREATE INDEX ix_ai_jobs_project_status ON ai_jobs (project_id, status);
CREATE INDEX ix_ai_jobs_status ON ai_jobs (status);
CREATE INDEX ix_ai_jobs_type_created ON ai_jobs (job_type, created_at);

-- ── Analysis results: the header of a manuscript analysis ──
CREATE TABLE analysis_results (
    id                     UUID PRIMARY KEY,
    ai_job_id              UUID          NOT NULL,
    project_id             UUID          NOT NULL,
    overall_confidence     NUMERIC(5, 2),
    summary                TEXT,
    language               VARCHAR(60),
    complexity_score       INT,
    complexity_label       VARCHAR(60),
    estimated_working_days INT,
    raw_payload            JSONB,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_analysis_results_job FOREIGN KEY (ai_job_id) REFERENCES ai_jobs (id),
    CONSTRAINT fk_analysis_results_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT ck_analysis_results_confidence CHECK
        (overall_confidence IS NULL OR (overall_confidence >= 0 AND overall_confidence <= 100)),
    CONSTRAINT ck_analysis_results_complexity CHECK
        (complexity_score IS NULL OR (complexity_score >= 0 AND complexity_score <= 100)),
    CONSTRAINT ck_analysis_results_days CHECK
        (estimated_working_days IS NULL OR estimated_working_days >= 0)
);
CREATE INDEX ix_analysis_results_job ON analysis_results (ai_job_id);
CREATE INDEX ix_analysis_results_project ON analysis_results (project_id, created_at DESC);
-- Flexible querying over the full model output kept for provenance.
CREATE INDEX gin_analysis_results_raw_payload ON analysis_results USING GIN (raw_payload);

-- ── Analysis metrics: the metric cards (pages, figures, equations, …) ──
CREATE TABLE analysis_metrics (
    id                 UUID PRIMARY KEY,
    analysis_result_id UUID         NOT NULL,
    metric_key         VARCHAR(60)  NOT NULL,
    metric_value       BIGINT,
    confidence         NUMERIC(5, 2),
    CONSTRAINT fk_analysis_metrics_result FOREIGN KEY (analysis_result_id)
        REFERENCES analysis_results (id) ON DELETE CASCADE,
    CONSTRAINT ck_analysis_metrics_confidence CHECK
        (confidence IS NULL OR (confidence >= 0 AND confidence <= 100))
);
CREATE INDEX ix_analysis_metrics_result ON analysis_metrics (analysis_result_id);

-- ── Analysis composition: donut segments (body / equations / figures / …) ──
CREATE TABLE analysis_composition (
    id                 UUID PRIMARY KEY,
    analysis_result_id UUID          NOT NULL,
    segment            VARCHAR(60)   NOT NULL,
    percentage         NUMERIC(5, 2) NOT NULL,
    CONSTRAINT fk_analysis_composition_result FOREIGN KEY (analysis_result_id)
        REFERENCES analysis_results (id) ON DELETE CASCADE,
    CONSTRAINT ck_analysis_composition_percentage CHECK (percentage BETWEEN 0 AND 100)
);
CREATE INDEX ix_analysis_composition_result ON analysis_composition (analysis_result_id);

-- ── Analysis headings: H1/H2/H3 counts ──
CREATE TABLE analysis_headings (
    id                 UUID PRIMARY KEY,
    analysis_result_id UUID        NOT NULL,
    level              VARCHAR(10) NOT NULL,
    count              INT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_analysis_headings_result FOREIGN KEY (analysis_result_id)
        REFERENCES analysis_results (id) ON DELETE CASCADE,
    CONSTRAINT ck_analysis_headings_count CHECK (count >= 0)
);
CREATE INDEX ix_analysis_headings_result ON analysis_headings (analysis_result_id);

-- ── Analysis risks: flagged risks with severity ──
CREATE TABLE analysis_risks (
    id                 UUID PRIMARY KEY,
    analysis_result_id UUID         NOT NULL,
    severity           VARCHAR(10)  NOT NULL,
    title              VARCHAR(250) NOT NULL,
    description        TEXT,
    CONSTRAINT fk_analysis_risks_result FOREIGN KEY (analysis_result_id)
        REFERENCES analysis_results (id) ON DELETE CASCADE,
    CONSTRAINT ck_analysis_risks_severity CHECK (severity IN ('HIGH', 'MEDIUM', 'LOW'))
);
CREATE INDEX ix_analysis_risks_result ON analysis_risks (analysis_result_id);

-- ── Team suggestions: AI-matched users (convertible into project_members) ──
CREATE TABLE team_suggestions (
    id                 UUID PRIMARY KEY,
    analysis_result_id UUID          NOT NULL,
    suggested_user_id  UUID,
    suggested_role     VARCHAR(40),
    match_score        NUMERIC(5, 2),
    rationale          TEXT,
    CONSTRAINT fk_team_suggestions_result FOREIGN KEY (analysis_result_id)
        REFERENCES analysis_results (id) ON DELETE CASCADE,
    CONSTRAINT fk_team_suggestions_user FOREIGN KEY (suggested_user_id) REFERENCES users (id),
    CONSTRAINT ck_team_suggestions_match_score CHECK
        (match_score IS NULL OR (match_score >= 0 AND match_score <= 100))
);
CREATE INDEX ix_team_suggestions_result ON team_suggestions (analysis_result_id);
CREATE INDEX ix_team_suggestions_user ON team_suggestions (suggested_user_id);

-- ============================================================================
-- No seed data: AI jobs and analysis rows are created at runtime by the AI
-- orchestration pipeline (Sprint 4 tasks). Consistent with V5/V6.
-- ============================================================================
