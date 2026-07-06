package com.protrack.preflight.web.dto;

/** Result of submitting a production PDF: the stored document/version and the new workflow stage. */
public record ProductionPdfResponse(
		String documentId,
		String versionId,
		String fileName,
		String stage) {
}
