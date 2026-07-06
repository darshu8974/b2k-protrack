package com.protrack.shared.events;

import java.util.UUID;

/**
 * Domain events published by the AI module and consumed by cross-cutting subscribers (audit) and
 * the SSE relay. Published within the originating transaction so audit rows are atomic with the
 * change (matching the project/file/package event convention).
 */
public final class AiEvents {

	private AiEvents() {
	}

	/** An AI job was created and queued for processing. */
	public record AiJobStarted(UUID organizationId, UUID projectId, UUID actorId, UUID jobId,
			String jobType) {
	}

	/** A manuscript analysis completed and its results were persisted. */
	public record AnalysisCompleted(UUID organizationId, UUID projectId, UUID actorId, UUID jobId,
			UUID analysisResultId, Integer overallConfidence) {
	}

	/** A PDF preflight completed and its run/checks/issues were persisted. */
	public record PreflightCompleted(UUID organizationId, UUID projectId, UUID actorId, UUID jobId,
			UUID preflightRunId, Integer overallScore, Boolean passed, Integer totalIssues,
			Integer highSeverity) {
	}

	/** An AI job failed after processing/retries. */
	public record AiJobFailed(UUID organizationId, UUID projectId, UUID actorId, UUID jobId,
			String jobType, String errorMessage) {
	}
}
