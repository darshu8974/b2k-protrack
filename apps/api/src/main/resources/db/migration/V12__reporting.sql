-- ============================================================================
-- Protrack — V12 reporting
-- ----------------------------------------------------------------------------
-- The analytics surface (Sprint 6) per the approved Database Design:
--   report_snapshots
--
-- Pre-computed periodic KPI values (turnaround, on-time %, AI confidence, QA
-- pass rate, throughput, workload-by-imprint). The /reports/* endpoints serve
-- LIVE aggregates off operational tables; this table is the historical/trend
-- store, written by the @Scheduled ReportSnapshotJob. Decoupling analytics from
-- live operational queries keeps dashboards fast (DB Design §"report_snapshots").
--
-- A snapshot is one (metric_key, dimension, period) measurement for an org:
--   metric_key = TURNAROUND_DAYS | ON_TIME_PCT | AVG_AI_CONFIDENCE | QA_PASS_PCT
--                | THROUGHPUT | WORKLOAD_BY_IMPRINT
--   dimension  = OVERALL for headline KPIs; a month (YYYY-MM) for THROUGHPUT;
--                an imprint label for WORKLOAD_BY_IMPRINT
--   metric_value = the numeric measurement (days / percentage / count)
--
-- Conventions (approved, matching V4–V11): UUID PK (app-generated); hard FK to
-- organization held with the default RESTRICT (no ON DELETE clause); metric_key
-- and dimension kept UNCONSTRAINED VARCHAR (data-driven, new metrics need no
-- migration — consistent with prior enums-as-strings where the set is app-owned);
-- generated_at defaults to now(). A UNIQUE(organization_id, metric_key, dimension,
-- period_start) key makes the snapshot job idempotent (upsert per measurement);
-- the Database Design's suggested index (org, metric_key, period_start) is
-- subsumed by this unique key. No seed — snapshots are produced at runtime by the
-- scheduled job (consistent with V5–V11).
-- ============================================================================

CREATE TABLE report_snapshots (
    id              UUID PRIMARY KEY,
    organization_id UUID          NOT NULL,
    period_start    DATE          NOT NULL,
    period_end      DATE          NOT NULL,
    metric_key      VARCHAR(60)   NOT NULL,
    dimension       VARCHAR(120)  NOT NULL DEFAULT 'OVERALL',
    metric_value    NUMERIC(14, 2) NOT NULL,
    generated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_report_snapshots_organization FOREIGN KEY (organization_id)
        REFERENCES organizations (id),
    -- One measurement per (org, metric, dimension, period) — the job upserts on this key.
    CONSTRAINT uq_report_snapshots_measurement
        UNIQUE (organization_id, metric_key, dimension, period_start)
);

-- The primary read path: an org's snapshots for a metric over time (DB Design §6).
CREATE INDEX ix_report_snapshots_org_metric
    ON report_snapshots (organization_id, metric_key, period_start);

-- ============================================================================
-- No seed data: snapshots are produced at runtime by the @Scheduled
-- ReportSnapshotJob. Consistent with V5/V6/V7/V8/V9/V10/V11.
-- ============================================================================
