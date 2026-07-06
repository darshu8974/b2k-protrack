package com.protrack.qa.web.dto;

import java.time.Instant;

/** A recorded triage decision, with the issue's resulting derived status. */
public record IssueDecisionResponse(
		String id,
		String issueId,
		String decision,
		String comment,
		String decidedBy,
		String decidedByName,
		String issueStatus,
		Instant createdAt) {
}
