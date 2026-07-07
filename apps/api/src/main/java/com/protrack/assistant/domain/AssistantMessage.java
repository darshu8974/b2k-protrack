package com.protrack.assistant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * One message in an assistant thread — either the user's question or the assistant's reply. The
 * {@code role} is stored as {@link MessageRole#name()}; {@code aiJobId} links to the originating AI
 * job when there is one (null for the synchronous chat path). {@code tokens} records the reply's
 * token usage when the provider reports it.
 *
 * <p>{@code createdAt} is set on the application clock so freshly persisted messages can be ordered
 * and returned in the same response (the DB {@code DEFAULT now()} is a fallback).
 */
@Entity
@Table(name = "assistant_messages")
public class AssistantMessage {

	@Id
	private UUID id;

	@Column(name = "thread_id", nullable = false)
	private UUID threadId;

	@Column(nullable = false)
	private String role;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	private Integer tokens;

	@Column(name = "ai_job_id")
	private UUID aiJobId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected AssistantMessage() {
	}

	public AssistantMessage(UUID id, UUID threadId, MessageRole role, String content, Integer tokens,
			UUID aiJobId, Instant now) {
		this.id = id;
		this.threadId = threadId;
		this.role = role.name();
		this.content = content;
		this.tokens = tokens;
		this.aiJobId = aiJobId;
		this.createdAt = now;
	}

	public UUID getId() {
		return id;
	}

	public UUID getThreadId() {
		return threadId;
	}

	public String getRole() {
		return role;
	}

	public String getContent() {
		return content;
	}

	public Integer getTokens() {
		return tokens;
	}

	public UUID getAiJobId() {
		return aiJobId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
