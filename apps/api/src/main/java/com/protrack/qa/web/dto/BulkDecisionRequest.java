package com.protrack.qa.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/** Apply one decision to many issues at once (fast triage; no per-issue comment). */
public record BulkDecisionRequest(
		@NotEmpty List<UUID> issueIds,
		@NotBlank String decision) {
}
