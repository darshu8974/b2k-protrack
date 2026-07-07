package com.protrack.assistant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A scoped assistant conversation for one (project, user) pair. Cross-entity references (project,
 * user) are held as UUIDs — the thread never navigates to those aggregates (module boundaries). One
 * thread per (project, user) is enforced by a DB unique constraint; the service get-or-creates it.
 *
 * <p>{@code createdAt} is set on the application clock so a freshly created thread can be returned in
 * the same response (the DB {@code DEFAULT now()} is a fallback), matching the comment/qa modules.
 */
@Entity
@Table(name = "assistant_threads")
public class AssistantThread {

	@Id
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected AssistantThread() {
	}

	public AssistantThread(UUID id, UUID projectId, UUID userId, Instant now) {
		this.id = id;
		this.projectId = projectId;
		this.userId = userId;
		this.createdAt = now;
	}

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public UUID getUserId() {
		return userId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
