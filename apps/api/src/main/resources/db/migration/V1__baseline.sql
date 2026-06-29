-- ============================================================================
-- Protrack — V1 baseline migration
-- ----------------------------------------------------------------------------
-- Establishes database extensions only. No business tables yet — those are
-- introduced per sprint (V2 identity, V3 projects/workflow, V4 audit, ...),
-- per the approved Database Design and Implementation Roadmap.
--
-- Convention reminders (approved): UUID v7 generated in the application layer;
-- enums stored as VARCHAR + CHECK; confidence values 0–100.
-- ============================================================================

-- pgcrypto: available for hashing and gen_random_uuid() if ever needed at the DB layer.
CREATE EXTENSION IF NOT EXISTS pgcrypto;
