package com.protrack.notification.domain;

/**
 * Delivery channels for a notification. Phase 1 supports the in-app bell and email; the enum is
 * stored as a name in {@code notifications.channel} (CHECK-constrained) and is extensible for
 * future channels (webhooks/Slack/Teams — Phase 2).
 */
public enum NotificationChannel {
	IN_APP,
	EMAIL
}
