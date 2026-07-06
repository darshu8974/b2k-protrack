package com.protrack.preflight.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One PDF-preflight execution against a production PDF version (one per successful
 * {@code PDF_PREFLIGHT} job). Owns the normalized {@link PreflightCheck} rows. The raised
 * {@code qa_issues} are a separate entity (they are the QA work surface, mutated by triage) and
 * reference the run by id. {@code createdAt} is set by the database default.
 */
@Entity
@Table(name = "preflight_runs")
public class PreflightRun {

	@Id
	private UUID id;

	@Column(name = "ai_job_id", nullable = false)
	private UUID aiJobId;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "pdf_version_id", nullable = false)
	private UUID pdfVersionId;

	private String standard;

	@Column(name = "overall_score")
	private Integer overallScore;

	private Boolean passed;

	@Column(name = "total_issues", nullable = false)
	private int totalIssues;

	@Column(name = "high_severity", nullable = false)
	private int highSeverity;

	@Column(nullable = false)
	private String status;

	@Column(name = "ran_at")
	private Instant ranAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "preflightRun", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PreflightCheck> checks = new ArrayList<>();

	protected PreflightRun() {
	}

	public PreflightRun(UUID id, UUID aiJobId, UUID projectId, UUID pdfVersionId, String standard,
			Integer overallScore, Boolean passed, int totalIssues, int highSeverity, String status,
			Instant ranAt) {
		this.id = id;
		this.aiJobId = aiJobId;
		this.projectId = projectId;
		this.pdfVersionId = pdfVersionId;
		this.standard = standard;
		this.overallScore = overallScore;
		this.passed = passed;
		this.totalIssues = totalIssues;
		this.highSeverity = highSeverity;
		this.status = status;
		this.ranAt = ranAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAiJobId() {
		return aiJobId;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public UUID getPdfVersionId() {
		return pdfVersionId;
	}

	public String getStandard() {
		return standard;
	}

	public Integer getOverallScore() {
		return overallScore;
	}

	public Boolean getPassed() {
		return passed;
	}

	public int getTotalIssues() {
		return totalIssues;
	}

	public int getHighSeverity() {
		return highSeverity;
	}

	public String getStatus() {
		return status;
	}

	public Instant getRanAt() {
		return ranAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public List<PreflightCheck> getChecks() {
		return checks;
	}
}
