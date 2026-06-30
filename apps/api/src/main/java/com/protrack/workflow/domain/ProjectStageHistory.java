package com.protrack.workflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** An immutable record of a single stage transition (append-only). */
@Entity
@Table(name = "project_stage_history")
public class ProjectStageHistory {

	@Id
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "from_stage")
	private String fromStage;

	@Column(name = "to_stage", nullable = false)
	private String toStage;

	@Column(name = "triggered_role")
	private String triggeredRole;

	@Column(name = "triggered_by")
	private UUID triggeredBy;

	@Column(columnDefinition = "text")
	private String note;

	@Column(name = "occurred_at", updatable = false)
	private Instant occurredAt;

	protected ProjectStageHistory() {
	}

	public ProjectStageHistory(UUID id, UUID projectId, String fromStage, String toStage,
			String triggeredRole, UUID triggeredBy, String note) {
		this.id = id;
		this.projectId = projectId;
		this.fromStage = fromStage;
		this.toStage = toStage;
		this.triggeredRole = triggeredRole;
		this.triggeredBy = triggeredBy;
		this.note = note;
		this.occurredAt = Instant.now();
	}

	public UUID getProjectId() {
		return projectId;
	}

	public String getFromStage() {
		return fromStage;
	}

	public String getToStage() {
		return toStage;
	}

	public String getTriggeredRole() {
		return triggeredRole;
	}

	public UUID getTriggeredBy() {
		return triggeredBy;
	}

	public String getNote() {
		return note;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}
}
