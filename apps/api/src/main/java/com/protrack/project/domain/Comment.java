package com.protrack.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A threaded, project-scoped comment. Cross-entity references (project, parent, author) are held as
 * UUIDs (no JPA associations — comments never navigate to those aggregates). The context
 * ({@code contextType} + {@code contextId}) is a loose polymorphic pointer to what the comment is
 * about (the project, a QA issue, a file…) — deliberately not FK-constrained. Soft-deleted via
 * {@code deletedAt}; the body/updatedAt are the only post-creation mutations (on edit).
 *
 * <p>Timestamps are set on the application clock so a freshly created/edited comment can be returned
 * in the same response (the DB {@code DEFAULT now()} is a fallback), matching the qa module.
 */
@Entity
@Table(name = "comments")
public class Comment {

	@Id
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "parent_comment_id")
	private UUID parentCommentId;

	@Column(name = "author_id", nullable = false)
	private UUID authorId;

	@Column(name = "context_type", nullable = false)
	private String contextType;

	@Column(name = "context_id")
	private UUID contextId;

	@Column(nullable = false, columnDefinition = "text")
	private String body;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	protected Comment() {
	}

	public Comment(UUID id, UUID projectId, UUID parentCommentId, UUID authorId, String contextType,
			UUID contextId, String body, Instant now) {
		this.id = id;
		this.projectId = projectId;
		this.parentCommentId = parentCommentId;
		this.authorId = authorId;
		this.contextType = contextType;
		this.contextId = contextId;
		this.body = body;
		this.createdAt = now;
		this.updatedAt = now;
	}

	/** Replace the body and bump {@code updatedAt} (marks the comment as edited). */
	public void editBody(String body, Instant when) {
		this.body = body;
		this.updatedAt = when;
	}

	/** Soft-delete: record the deletion time; the row is retained for history. */
	public void softDelete(Instant when) {
		this.deletedAt = when;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public boolean isEdited() {
		return updatedAt.isAfter(createdAt);
	}

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public UUID getParentCommentId() {
		return parentCommentId;
	}

	public UUID getAuthorId() {
		return authorId;
	}

	public String getContextType() {
		return contextType;
	}

	public UUID getContextId() {
		return contextId;
	}

	public String getBody() {
		return body;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}
}
