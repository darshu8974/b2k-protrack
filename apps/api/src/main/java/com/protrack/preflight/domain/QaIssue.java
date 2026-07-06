package com.protrack.preflight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * An issue raised by a preflight run — the core QA work surface. Created by the preflight module,
 * triaged by the qa module (which records decisions and mutates {@code status} via the
 * {@link com.protrack.preflight.spi.PreflightFacade}). References its run by id (cross-entity FK
 * kept as a plain UUID, per the module-boundary convention). {@code createdAt} is a DB default.
 */
@Entity
@Table(name = "qa_issues")
public class QaIssue {

	@Id
	private UUID id;

	@Column(name = "preflight_run_id", nullable = false)
	private UUID preflightRunId;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	private String category;

	@Column(nullable = false)
	private String severity;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "text")
	private String recommendation;

	@Column(name = "page_ref")
	private String pageRef;

	@Column(nullable = false)
	private String source;

	@Column(nullable = false)
	private String status;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", insertable = false)
	private Instant updatedAt;

	protected QaIssue() {
	}

	public QaIssue(UUID id, UUID preflightRunId, UUID projectId, String category, String severity,
			String title, String recommendation, String pageRef, String source, String status) {
		this.id = id;
		this.preflightRunId = preflightRunId;
		this.projectId = projectId;
		this.category = category;
		this.severity = severity;
		this.title = title;
		this.recommendation = recommendation;
		this.pageRef = pageRef;
		this.source = source;
		this.status = status;
	}

	/** Update the derived triage status (the decision history is the system of record). */
	public void applyStatus(String status) {
		this.status = status;
		this.updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getPreflightRunId() {
		return preflightRunId;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public String getCategory() {
		return category;
	}

	public String getSeverity() {
		return severity;
	}

	public String getTitle() {
		return title;
	}

	public String getRecommendation() {
		return recommendation;
	}

	public String getPageRef() {
		return pageRef;
	}

	public String getSource() {
		return source;
	}

	public String getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
