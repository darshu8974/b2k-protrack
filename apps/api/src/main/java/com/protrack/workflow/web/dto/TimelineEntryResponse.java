package com.protrack.workflow.web.dto;

import java.time.Instant;

/** One entry in a project's workflow timeline. */
public record TimelineEntryResponse(
		String fromStage,
		String toStage,
		String triggeredRole,
		String triggeredByName,
		String note,
		Instant occurredAt) {
}
