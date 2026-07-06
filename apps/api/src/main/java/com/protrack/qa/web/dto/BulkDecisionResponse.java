package com.protrack.qa.web.dto;

import java.util.List;

/** Summary of a bulk triage: how many issues were decided and their resulting status. */
public record BulkDecisionResponse(
		int decided,
		String decision,
		String issueStatus,
		List<String> issueIds) {
}
