package com.protrack.qa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * An append-only QA triage decision on a preflight issue (ACCEPT_FIX / SEND_BACK / COMMENT). The
 * decision trail is the system of record; the issue's {@code status} is a derived convenience.
 * References the issue by id (cross-module FK kept as a plain UUID). {@code createdAt} is a DB default.
 */
@Entity
@Table(name = "qa_issue_decisions")
public class QaIssueDecision {

	@Id
	private UUID id;

	@Column(name = "issue_id", nullable = false)
	private UUID issueId;

	@Column(name = "decided_by")
	private UUID decidedBy;

	@Column(nullable = false)
	private String decision;

	@Column(columnDefinition = "text")
	private String comment;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	protected QaIssueDecision() {
	}

	public QaIssueDecision(UUID id, UUID issueId, UUID decidedBy, String decision, String comment) {
		this.id = id;
		this.issueId = issueId;
		this.decidedBy = decidedBy;
		this.decision = decision;
		this.comment = comment;
	}

	public UUID getId() {
		return id;
	}

	public UUID getIssueId() {
		return issueId;
	}

	public UUID getDecidedBy() {
		return decidedBy;
	}

	public String getDecision() {
		return decision;
	}

	public String getComment() {
		return comment;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
