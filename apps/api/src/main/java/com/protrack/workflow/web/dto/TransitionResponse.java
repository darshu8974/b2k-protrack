package com.protrack.workflow.web.dto;

import java.time.Instant;

/** Result of a stage transition. */
public record TransitionResponse(
		String projectId,
		String fromStage,
		String toStage,
		String triggeredRole,
		boolean approvalGate,
		Instant occurredAt) {
}
