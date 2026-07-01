package com.protrack.files.domain;

/**
 * Logical document types. Stored as a string (the {@code documents.doc_type} column is intentionally
 * unconstrained so new formats need no migration — see the Database Design). This enum enumerates the
 * types the application understands today and drives per-type upload validation.
 */
public enum DocType {

	/** The authored manuscript (DOCX/PDF) — the Phase 1 upload focus. */
	MANUSCRIPT,

	/** The final production PDF produced in InDesign (ingested in a later sprint). */
	PRODUCTION_PDF,

	/** Structured XML extracted from the manuscript. */
	STRUCTURED_XML,

	/** A manifest of figures/assets. */
	FIGURES_MANIFEST,

	/** Any other supporting artifact. */
	OTHER
}
