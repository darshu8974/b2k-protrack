package com.protrack.files.web.dto;

import java.time.Instant;

/** Full document detail: identity, status, and its current version. */
public record DocumentResponse(
		String id,
		String projectId,
		String docType,
		String title,
		String status,
		FileVersionResponse currentVersion,
		Instant createdAt,
		Instant updatedAt) {
}
