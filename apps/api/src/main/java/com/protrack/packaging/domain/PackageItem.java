package com.protrack.packaging.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * A single entry in a {@link ProductionPackage}: a labeled reference to a document (deliverable),
 * with a size snapshot and display order. The document is a cross-module reference held as a UUID.
 */
@Entity
@Table(name = "package_items")
public class PackageItem {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "package_id", nullable = false)
	private ProductionPackage productionPackage;

	@Column(name = "document_id")
	private UUID documentId;

	@Column(name = "item_type", nullable = false)
	private String itemType;

	@Column(nullable = false)
	private String label;

	@Column(name = "size_bytes")
	private Long sizeBytes;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	protected PackageItem() {
	}

	public PackageItem(UUID id, ProductionPackage productionPackage, UUID documentId, String itemType,
			String label, Long sizeBytes, int sortOrder) {
		this.id = id;
		this.productionPackage = productionPackage;
		this.documentId = documentId;
		this.itemType = itemType;
		this.label = label;
		this.sizeBytes = sizeBytes;
		this.sortOrder = sortOrder;
	}

	public UUID getId() {
		return id;
	}

	public UUID getDocumentId() {
		return documentId;
	}

	public String getItemType() {
		return itemType;
	}

	public String getLabel() {
		return label;
	}

	public Long getSizeBytes() {
		return sizeBytes;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
