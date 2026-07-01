package com.protrack.files.web.dto;

import java.time.Instant;

/** Document row for the project documents list, with its current version and version count. */
public record DocumentSummaryResponse(
		String id,
		String docType,
		String title,
		String status,
		int versionCount,
		FileVersionResponse currentVersion,
		Instant createdAt,
		Instant updatedAt) {
}
