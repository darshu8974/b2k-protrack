package com.protrack.project.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for PATCH /api/v1/comments/{id} — edit a comment's text. */
public record UpdateCommentRequest(
		@NotBlank @Size(max = 4000) String body) {
}
