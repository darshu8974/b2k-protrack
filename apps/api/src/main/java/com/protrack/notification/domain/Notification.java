package com.protrack.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A per-recipient notification: an in-app feed item (the bell). Read state ({@code isRead} /
 * {@code readAt}) is the only mutation; {@code sentAt} is stamped when the same event was also
 * delivered by email. Created only by the notification pipeline; otherwise append-only.
 */
@Entity
@Table(name = "notifications")
public class Notification {

	@Id
	private UUID id;

	@Column(name = "recipient_id", nullable = false)
	private UUID recipientId;

	@Column(name = "related_project_id")
	private UUID relatedProjectId;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false)
	private String title;

	@Column
	private String body;

	@Column(nullable = false)
	private String channel;

	@Column(name = "related_entity_type")
	private String relatedEntityType;

	@Column(name = "related_entity_id")
	private UUID relatedEntityId;

	@Column(name = "is_read", nullable = false)
	private boolean read;

	@Column(name = "read_at")
	private Instant readAt;

	@Column(name = "sent_at")
	private Instant sentAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	protected Notification() {
	}

	public Notification(UUID id, UUID recipientId, UUID relatedProjectId, NotificationType type,
			String title, String body, NotificationChannel channel, String relatedEntityType,
			UUID relatedEntityId, Instant sentAt) {
		this.id = id;
		this.recipientId = recipientId;
		this.relatedProjectId = relatedProjectId;
		this.type = type.name();
		this.title = title;
		this.body = body;
		this.channel = channel.name();
		this.relatedEntityType = relatedEntityType;
		this.relatedEntityId = relatedEntityId;
		this.read = false;
		this.sentAt = sentAt;
	}

	/** Mark this notification read (idempotent). */
	public void markRead(Instant when) {
		if (!this.read) {
			this.read = true;
			this.readAt = when;
		}
	}

	public UUID getId() {
		return id;
	}

	public UUID getRecipientId() {
		return recipientId;
	}

	public UUID getRelatedProjectId() {
		return relatedProjectId;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public String getChannel() {
		return channel;
	}

	public String getRelatedEntityType() {
		return relatedEntityType;
	}

	public UUID getRelatedEntityId() {
		return relatedEntityId;
	}

	public boolean isRead() {
		return read;
	}

	public Instant getReadAt() {
		return readAt;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
