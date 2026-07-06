package com.protrack.qa.web.dto;

import java.time.Instant;

/** An entry in a project's approval-gate history. */
public record ApprovalResponse(
		String id,
		String projectId,
		String stageCode,
		String approvalType,
		String decision,
		String decidedRole,
		String decidedBy,
		String decidedByName,
		String comment,
		Instant createdAt) {
}
