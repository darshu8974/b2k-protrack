package com.protrack.project.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Body for POST /api/v1/projects/{id}/comments. {@code contextType}/{@code contextId} default to
 * the project itself when omitted (general project discussion); {@code parentId} makes this a reply.
 */
public record CreateCommentRequest(
		@NotBlank @Size(max = 4000) String body,
		UUID parentId,
		@Size(max = 40) String contextType,
		UUID contextId) {
}
