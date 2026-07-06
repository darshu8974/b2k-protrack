package com.protrack.notification.web.dto;

import java.time.Instant;

/** A single notification feed item. */
public record NotificationResponse(
		String id,
		String type,
		String title,
		String body,
		String projectId,
		String relatedEntityType,
		String relatedEntityId,
		boolean read,
		Instant readAt,
		Instant sentAt,
		Instant createdAt) {
}
