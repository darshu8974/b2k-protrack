package com.protrack.files.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A logical document within a project — a stable identity across many immutable {@link FileVersion}s.
 * Cross-module references (project, users) and the current-version pointer are held as UUIDs.
 * {@code createdAt} is populated by the database default; other timestamps by lifecycle callbacks.
 */
@Entity
@Table(name = "documents")
public class Document {

	@Id
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "doc_type", nullable = false)
	private String docType;

	private String title;

	@Column(name = "current_version_id")
	private UUID currentVersionId;

	@Column(nullable = false)
	private String status;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "created_by")
	private UUID createdBy;

	@Column(name = "updated_by")
	private UUID updatedBy;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	protected Document() {
	}

	public Document(UUID id, UUID projectId, DocType docType, String title, UUID createdBy) {
		this.id = id;
		this.projectId = projectId;
		this.docType = docType.name();
		this.title = title;
		this.status = DocumentStatus.ACTIVE.name();
		this.createdBy = createdBy;
		this.updatedBy = createdBy;
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

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public String getDocType() {
		return docType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public UUID getCurrentVersionId() {
		return currentVersionId;
	}

	public void setCurrentVersionId(UUID currentVersionId) {
		this.currentVersionId = currentVersionId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(DocumentStatus status) {
		this.status = status.name();
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public UUID getCreatedBy() {
		return createdBy;
	}

	public void setUpdatedBy(UUID updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Instant deletedAt) {
		this.deletedAt = deletedAt;
	}
}
