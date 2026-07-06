package com.protrack.notification.web.dto;

/** A user's effective channel preference for one notification type. */
public record NotificationPreferenceResponse(
		String type,
		String label,
		boolean inAppEnabled,
		boolean emailEnabled) {
}
