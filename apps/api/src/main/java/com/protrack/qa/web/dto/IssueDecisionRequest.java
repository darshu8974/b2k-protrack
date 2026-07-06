package com.protrack.qa.web.dto;

import jakarta.validation.constraints.NotBlank;

/** A QA triage decision on a single issue. {@code comment} is required for SEND_BACK / COMMENT. */
public record IssueDecisionRequest(
		@NotBlank String decision,
		String comment) {
}
