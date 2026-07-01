package com.protrack.packaging.web.dto;

/** A single production-package content entry. */
public record PackageItemResponse(
		String id,
		String documentId,
		String itemType,
		String label,
		Long sizeBytes,
		int sortOrder) {
}
