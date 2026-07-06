package com.protrack.notification.service;

import com.protrack.notification.domain.NotificationType;
import java.util.List;
import java.util.UUID;

/**
 * A fan-out instruction built by the {@code NotificationEventListener} from a domain event: what to
 * tell whom. The {@link com.protrack.notification.service.NotificationService} resolves each
 * recipient's channel preferences and delivers accordingly.
 *
 * @param type the notification type (drives preferences + labelling)
 * @param recipientIds the users to notify (already excludes the actor)
 * @param title short headline
 * @param body longer text (also the email body)
 * @param projectId the related project (nullable)
 * @param relatedEntityType polymorphic link target type (nullable)
 * @param relatedEntityId polymorphic link target id (nullable)
 */
public record NotificationRequest(
		NotificationType type,
		List<UUID> recipientIds,
		String title,
		String body,
		UUID projectId,
		String relatedEntityType,
		UUID relatedEntityId) {
}
