package com.protrack.qa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * An append-only human approval/decision at a workflow gate (e.g. the QA final sign-off). Distinct
 * from the formal {@link QaSignoff} e-signature: {@code approvals} is the unified, queryable gate
 * history across the pipeline. {@code createdAt} is set by the database default.
 */
@Entity
@Table(name = "approvals")
public class Approval {

	@Id
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "stage_code")
	private String stageCode;

	@Column(name = "approval_type")
	private String approvalType;

	@Column(nullable = false)
	private String decision;

	@Column(name = "decided_role")
	private String decidedRole;

	@Column(name = "decided_by")
	private UUID decidedBy;

	@Column(columnDefinition = "text")
	private String comment;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	protected Approval() {
	}

	public Approval(UUID id, UUID projectId, String stageCode, String approvalType, String decision,
			String decidedRole, UUID decidedBy, String comment) {
		this.id = id;
		this.projectId = projectId;
		this.stageCode = stageCode;
		this.approvalType = approvalType;
		this.decision = decision;
		this.decidedRole = decidedRole;
		this.decidedBy = decidedBy;
		this.comment = comment;
	}

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public String getStageCode() {
		return stageCode;
	}

	public String getApprovalType() {
		return approvalType;
	}

	public String getDecision() {
		return decision;
	}

	public String getDecidedRole() {
		return decidedRole;
	}

	public UUID getDecidedBy() {
		return decidedBy;
	}

	public String getComment() {
		return comment;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
