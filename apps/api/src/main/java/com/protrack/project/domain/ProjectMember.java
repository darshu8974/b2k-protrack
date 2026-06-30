package com.protrack.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** A user assigned to a project (cross-module user reference held as a UUID). */
@Entity
@Table(name = "project_members")
public class ProjectMember {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "role_in_project")
	private String roleInProject;

	@Column(name = "is_owner", nullable = false)
	private boolean owner;

	@Column(name = "match_score")
	private BigDecimal matchScore;

	@Column(name = "assigned_at", insertable = false, updatable = false)
	private Instant assignedAt;

	@Column(name = "assigned_by")
	private UUID assignedBy;

	protected ProjectMember() {
	}

	public ProjectMember(UUID id, Project project, UUID userId, String roleInProject, boolean owner,
			BigDecimal matchScore, UUID assignedBy) {
		this.id = id;
		this.project = project;
		this.userId = userId;
		this.roleInProject = roleInProject;
		this.owner = owner;
		this.matchScore = matchScore;
		this.assignedBy = assignedBy;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getRoleInProject() {
		return roleInProject;
	}

	public boolean isOwner() {
		return owner;
	}

	public BigDecimal getMatchScore() {
		return matchScore;
	}

	public Instant getAssignedAt() {
		return assignedAt;
	}

	public void setRoleInProject(String roleInProject) {
		this.roleInProject = roleInProject;
	}

	public void setMatchScore(BigDecimal matchScore) {
		this.matchScore = matchScore;
	}
}
