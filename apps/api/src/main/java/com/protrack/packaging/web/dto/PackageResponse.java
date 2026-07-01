package com.protrack.packaging.web.dto;

import java.time.Instant;
import java.util.List;

/** A production package with its contents and hand-off metadata. */
public record PackageResponse(
		String id,
		String projectId,
		String status,
		long totalSizeBytes,
		int itemCount,
		int downloadCount,
		Instant assembledAt,
		String assembledById,
		String assembledByName,
		Instant createdAt,
		Instant updatedAt,
		List<PackageItemResponse> items) {
}
