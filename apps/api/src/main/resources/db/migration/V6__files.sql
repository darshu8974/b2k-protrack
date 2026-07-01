-- ============================================================================
-- Protrack — V6 files, versioning & production package
-- ----------------------------------------------------------------------------
-- Document management backbone per the approved Database Design:
--   documents · file_versions · production_packages · package_items
--
-- Design principles honored:
--   * Versioning is first-class — a logical `documents` row points at many
--     immutable `file_versions`; exactly one is flagged current (partial-unique).
--   * The DB stores only file *metadata* (storage_key, size, checksum); the
--     binaries live in object storage (StoragePort / local-disk adapter).
--   * Production packages are metadata + asset references only — no IDML/layout
--     generation (the InDesign boundary is respected, Phase 1 rule).
--
-- Conventions (approved, matching V4): UUID PK for exposed entities
-- (app-generated v7; no seeds needed here); enums as VARCHAR + CHECK; audit
-- columns on mutable tables; sizes as BIGINT (bytes). `doc_type`/`item_type`
-- are intentionally left unconstrained VARCHAR — the design keeps document
-- formats "data-driven" so new types need no schema change (see Database
-- Design §Future-proofing). No entities/services/APIs in this task — schema only.
-- ============================================================================

-- ── Documents: a logical document within a project (stable identity) ──
-- doc_type examples: MANUSCRIPT, PRODUCTION_PDF, STRUCTURED_XML, FIGURES_MANIFEST.
-- `current_version_id` is a denormalized pointer to the active file_version; the
-- authoritative "one current per document" rule is enforced by the partial-unique
-- index on file_versions.is_current below. FK added after file_versions exists
-- (the two tables reference each other).
CREATE TABLE documents (
    id                 UUID PRIMARY KEY,
    project_id         UUID         NOT NULL,
    doc_type           VARCHAR(40)  NOT NULL,
    title              VARCHAR(400),
    current_version_id UUID,
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         UUID,
    updated_by         UUID,
    deleted_at         TIMESTAMPTZ,
    CONSTRAINT fk_documents_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT ck_documents_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);
CREATE INDEX ix_documents_project ON documents (project_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_documents_project_type ON documents (project_id, doc_type);

-- ── File versions: immutable physical versions of a document ──
-- Never mutated after insert. A new upload creates a new row (version_no = max+1)
-- and flips is_current. checksum_sha256 gives integrity + duplicate detection.
CREATE TABLE file_versions (
    id              UUID PRIMARY KEY,
    document_id     UUID         NOT NULL,
    version_no      INT          NOT NULL,
    file_name       VARCHAR(400) NOT NULL,
    mime_type       VARCHAR(160) NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    storage_key     VARCHAR(512) NOT NULL,
    checksum_sha256 VARCHAR(64),
    is_current      BOOLEAN      NOT NULL DEFAULT FALSE,
    uploaded_by     UUID,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_file_versions_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_file_versions_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id),
    CONSTRAINT uq_file_versions_document_version UNIQUE (document_id, version_no),
    CONSTRAINT ck_file_versions_version_no CHECK (version_no > 0),
    CONSTRAINT ck_file_versions_size_bytes CHECK (size_bytes >= 0)
);
CREATE INDEX ix_file_versions_document ON file_versions (document_id);
-- Exactly one current version per document.
CREATE UNIQUE INDEX ux_file_versions_current ON file_versions (document_id) WHERE is_current;

-- Now that file_versions exists, wire the documents -> current version pointer.
ALTER TABLE documents
    ADD CONSTRAINT fk_documents_current_version
    FOREIGN KEY (current_version_id) REFERENCES file_versions (id);

-- ── Production packages: the assembled hand-off bundle metadata ──
-- Tracks the hand-off (status, size, item/download counts) without generating IDML.
CREATE TABLE production_packages (
    id               UUID PRIMARY KEY,
    project_id       UUID         NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    total_size_bytes BIGINT       NOT NULL DEFAULT 0,
    item_count       INT          NOT NULL DEFAULT 0,
    download_count   INT          NOT NULL DEFAULT 0,
    assembled_at     TIMESTAMPTZ,
    assembled_by     UUID,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_production_packages_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_production_packages_assembled_by FOREIGN KEY (assembled_by) REFERENCES users (id),
    CONSTRAINT ck_production_packages_status CHECK (status IN ('DRAFT', 'ASSEMBLING', 'ASSEMBLED', 'FAILED')),
    CONSTRAINT ck_production_packages_total_size CHECK (total_size_bytes >= 0),
    CONSTRAINT ck_production_packages_item_count CHECK (item_count >= 0),
    CONSTRAINT ck_production_packages_download_count CHECK (download_count >= 0)
);
CREATE INDEX ix_production_packages_project ON production_packages (project_id);

-- ── Package items: normalized contents of a production package ──
-- Each item references a document (the deliverable) with a display label and order.
-- item_type is left data-driven (unconstrained) to mirror doc_type.
CREATE TABLE package_items (
    id          UUID PRIMARY KEY,
    package_id  UUID         NOT NULL,
    document_id UUID,
    item_type   VARCHAR(40)  NOT NULL,
    label       VARCHAR(400) NOT NULL,
    size_bytes  BIGINT,
    sort_order  INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_package_items_package FOREIGN KEY (package_id) REFERENCES production_packages (id) ON DELETE CASCADE,
    CONSTRAINT fk_package_items_document FOREIGN KEY (document_id) REFERENCES documents (id),
    CONSTRAINT ck_package_items_size_bytes CHECK (size_bytes IS NULL OR size_bytes >= 0)
);
CREATE INDEX ix_package_items_package ON package_items (package_id, sort_order);
CREATE INDEX ix_package_items_document ON package_items (document_id);

-- ============================================================================
-- No seed data: documents, file versions and packages are created at runtime
-- (upload / assembly). Consistent with V5 (audit) — reference/seed rows for the
-- pipeline live in V4 (workflow_stages, imprints).
-- ============================================================================
