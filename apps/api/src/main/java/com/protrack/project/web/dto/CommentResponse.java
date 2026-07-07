package com.protrack.project.web.dto;

import java.time.Instant;

/** A single comment with its author resolved for display. */
public record CommentResponse(
		String id,
		String projectId,
		String parentId,
		String authorId,
		String authorName,
		String authorInitials,
		String authorColor,
		String contextType,
		String contextId,
		String body,
		boolean edited,
		Instant createdAt,
		Instant updatedAt) {
}
