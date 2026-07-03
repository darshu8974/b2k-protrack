package com.protrack.ai.web.dto;

import com.protrack.ai.domain.AiJob;
import java.time.Instant;

/** AI job status/progress returned by start (202) and GET /ai-jobs/{id}. */
public record AiJobResponse(
		String jobId,
		String projectId,
		String jobType,
		String status,
		int progressPct,
		String provider,
		String model,
		String errorMessage,
		Instant createdAt,
		Instant finishedAt) {

	public static AiJobResponse from(AiJob job) {
		return new AiJobResponse(
				job.getId().toString(),
				job.getProjectId().toString(),
				job.getJobType(),
				job.getStatus(),
				job.getProgressPct(),
				job.getProvider(),
				job.getModel(),
				job.getErrorMessage(),
				job.getCreatedAt(),
				job.getFinishedAt());
	}
}
