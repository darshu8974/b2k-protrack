package com.protrack.qa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * The formal QA e-signature attestation (append-only, non-repudiation). One row per sign-off event;
 * a rejection also writes a row. Certifies a specific preflight run. {@code createdAt} is a DB default.
 */
@Entity
@Table(name = "qa_signoffs")
public class QaSignoff {

	@Id
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "preflight_run_id", nullable = false)
	private UUID preflightRunId;

	@Column(name = "signed_by")
	private UUID signedBy;

	@Column(nullable = false)
	private String decision;

	@Column(name = "quality_score")
	private Integer qualityScore;

	@Column(name = "signature_hash")
	private String signatureHash;

	@Column(name = "artifact_checksum")
	private String artifactChecksum;

	@Column(columnDefinition = "text")
	private String notes;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	protected QaSignoff() {
	}

	public QaSignoff(UUID id, UUID projectId, UUID preflightRunId, UUID signedBy, String decision,
			Integer qualityScore, String signatureHash, String artifactChecksum, String notes) {
		this.id = id;
		this.projectId = projectId;
		this.preflightRunId = preflightRunId;
		this.signedBy = signedBy;
		this.decision = decision;
		this.qualityScore = qualityScore;
		this.signatureHash = signatureHash;
		this.artifactChecksum = artifactChecksum;
		this.notes = notes;
	}

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public UUID getPreflightRunId() {
		return preflightRunId;
	}

	public UUID getSignedBy() {
		return signedBy;
	}

	public String getDecision() {
		return decision;
	}

	public Integer getQualityScore() {
		return qualityScore;
	}

	public String getSignatureHash() {
		return signatureHash;
	}

	public String getArtifactChecksum() {
		return artifactChecksum;
	}

	public String getNotes() {
		return notes;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
