package com.protrack.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * A user's per-type channel opt-in. Absence of a row means both channels are enabled (default-on);
 * a row is created lazily the first time a user overrides a default. Unique on (user_id, type).
 */
@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(nullable = false)
	private String type;

	@Column(name = "in_app_enabled", nullable = false)
	private boolean inAppEnabled;

	@Column(name = "email_enabled", nullable = false)
	private boolean emailEnabled;

	protected NotificationPreference() {
	}

	public NotificationPreference(UUID id, UUID userId, NotificationType type, boolean inAppEnabled,
			boolean emailEnabled) {
		this.id = id;
		this.userId = userId;
		this.type = type.name();
		this.inAppEnabled = inAppEnabled;
		this.emailEnabled = emailEnabled;
	}

	public void update(boolean inAppEnabled, boolean emailEnabled) {
		this.inAppEnabled = inAppEnabled;
		this.emailEnabled = emailEnabled;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getType() {
		return type;
	}

	public boolean isInAppEnabled() {
		return inAppEnabled;
	}

	public boolean isEmailEnabled() {
		return emailEnabled;
	}
}
