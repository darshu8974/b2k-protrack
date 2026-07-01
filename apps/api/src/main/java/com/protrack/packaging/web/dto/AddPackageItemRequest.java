package com.protrack.packaging.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Curate a package by adding a document as an item. {@code label} and {@code itemType} default to the
 * document's title and type when omitted; {@code sortOrder} defaults to the end of the list.
 */
public record AddPackageItemRequest(
		@NotNull UUID documentId,
		String itemType,
		String label,
		Integer sortOrder) {
}
