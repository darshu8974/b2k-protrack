package com.protrack.ai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * An async AI execution. Spring owns all job state; the FastAPI service is a stateless reporter.
 * Lifecycle: QUEUED → RUNNING → SUCCEEDED | FAILED. {@code createdAt} is set by the database default.
 */
@Entity
@Table(name = "ai_jobs")
public class AiJob {

	@Id
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "job_type", nullable = false)
	private String jobType;

	@Column(nullable = false)
	private String status;

	@Column(name = "progress_pct", nullable = false)
	private int progressPct;

	private String provider;

	private String model;

	@Column(name = "input_version_id")
	private UUID inputVersionId;

	@Column(name = "started_at")
	private Instant startedAt;

	@Column(name = "finished_at")
	private Instant finishedAt;

	@Column(name = "duration_ms")
	private Integer durationMs;

	@Column(name = "error_message", columnDefinition = "text")
	private String errorMessage;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "created_by")
	private UUID createdBy;

	protected AiJob() {
	}

	public AiJob(UUID id, UUID projectId, JobType jobType, UUID inputVersionId, UUID createdBy) {
		this.id = id;
		this.projectId = projectId;
		this.jobType = jobType.name();
		this.status = JobStatus.QUEUED.name();
		this.progressPct = 0;
		this.inputVersionId = inputVersionId;
		this.createdBy = createdBy;
	}

	public void markRunning() {
		this.status = JobStatus.RUNNING.name();
		if (this.startedAt == null) {
			this.startedAt = Instant.now();
		}
	}

	public void markSucceeded(String provider, String model) {
		this.status = JobStatus.SUCCEEDED.name();
		this.progressPct = 100;
		this.provider = provider;
		this.model = model;
		finish();
	}

	public void markFailed(String errorMessage) {
		this.status = JobStatus.FAILED.name();
		this.errorMessage = errorMessage;
		finish();
	}

	private void finish() {
		this.finishedAt = Instant.now();
		if (this.startedAt != null) {
			this.durationMs = (int) Math.min(
					Integer.MAX_VALUE, this.finishedAt.toEpochMilli() - this.startedAt.toEpochMilli());
		}
	}

	public void updateProgress(int progressPct, JobStatus status) {
		this.progressPct = progressPct;
		if (status != null) {
			this.status = status.name();
		}
	}

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public String getJobType() {
		return jobType;
	}

	public String getStatus() {
		return status;
	}

	public int getProgressPct() {
		return progressPct;
	}

	public String getProvider() {
		return provider;
	}

	public String getModel() {
		return model;
	}

	public UUID getInputVersionId() {
		return inputVersionId;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public Instant getFinishedAt() {
		return finishedAt;
	}

	public Integer getDurationMs() {
		return durationMs;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public UUID getCreatedBy() {
		return createdBy;
	}
}
