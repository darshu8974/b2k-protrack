package com.protrack.workflow.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Body for POST /api/v1/projects/{id}/transitions. */
public record TransitionRequest(
		@NotBlank String toStage,
		String note) {
}
