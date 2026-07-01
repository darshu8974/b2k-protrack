package com.protrack.files.web.dto;

import java.time.Instant;

/** A single immutable file version. */
public record FileVersionResponse(
		String id,
		int versionNo,
		String fileName,
		String mimeType,
		long sizeBytes,
		String checksumSha256,
		boolean current,
		String uploadedById,
		String uploadedByName,
		Instant createdAt) {
}
