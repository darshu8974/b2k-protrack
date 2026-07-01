package com.protrack.packaging.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The assembled production hand-off bundle metadata for a project (no IDML generation — the
 * package is metadata plus references to existing document versions). {@code createdAt} is set by
 * the database default; other timestamps by lifecycle callbacks.
 */
@Entity
@Table(name = "production_packages")
public class ProductionPackage {

	@Id
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(nullable = false)
	private String status;

	@Column(name = "total_size_bytes", nullable = false)
	private long totalSizeBytes;

	@Column(name = "item_count", nullable = false)
	private int itemCount;

	@Column(name = "download_count", nullable = false)
	private int downloadCount;

	@Column(name = "assembled_at")
	private Instant assembledAt;

	@Column(name = "assembled_by")
	private UUID assembledBy;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "productionPackage", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC")
	private List<PackageItem> items = new ArrayList<>();

	protected ProductionPackage() {
	}

	public ProductionPackage(UUID id, UUID projectId) {
		this.id = id;
		this.projectId = projectId;
		this.status = PackageStatus.DRAFT.name();
	}

	@PrePersist
	void onCreate() {
		if (updatedAt == null) {
			updatedAt = Instant.now();
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	/** Replace all items and recompute derived totals (item count + total size). */
	public void replaceItems(List<PackageItem> newItems) {
		items.clear();
		items.addAll(newItems);
		recomputeTotals();
	}

	public void addItem(PackageItem item) {
		items.add(item);
		recomputeTotals();
	}

	public void removeItem(PackageItem item) {
		items.remove(item);
		recomputeTotals();
	}

	public void recomputeTotals() {
		this.itemCount = items.size();
		this.totalSizeBytes = items.stream()
				.map(PackageItem::getSizeBytes).filter(java.util.Objects::nonNull)
				.mapToLong(Long::longValue).sum();
	}

	public void markAssembled(UUID assembledBy, Instant when) {
		this.status = PackageStatus.ASSEMBLED.name();
		this.assembledBy = assembledBy;
		this.assembledAt = when;
	}

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(PackageStatus status) {
		this.status = status.name();
	}

	public long getTotalSizeBytes() {
		return totalSizeBytes;
	}

	public int getItemCount() {
		return itemCount;
	}

	public int getDownloadCount() {
		return downloadCount;
	}

	public void incrementDownloadCount() {
		this.downloadCount++;
	}

	public Instant getAssembledAt() {
		return assembledAt;
	}

	public UUID getAssembledBy() {
		return assembledBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public List<PackageItem> getItems() {
		return items;
	}
}
