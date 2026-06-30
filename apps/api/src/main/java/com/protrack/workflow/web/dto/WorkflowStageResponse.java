package com.protrack.workflow.web.dto;

/** A pipeline stage in the reference list. */
public record WorkflowStageResponse(
		String code,
		String name,
		int sequence,
		String description) {
}
