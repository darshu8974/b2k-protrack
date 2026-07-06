package com.protrack.notification.domain;

/**
 * The kinds of notification Protrack fans out in Phase 1 — the workflow/pipeline milestones a
 * project's team cares about. Stored as the enum name in {@code notifications.type} and
 * {@code notification_preferences.type} (VARCHAR); new types are additive.
 *
 * <p>{@code label} is the human-readable name surfaced in the preferences list.
 */
public enum NotificationType {

	STAGE_CHANGED("Workflow stage changes"),
	ANALYSIS_COMPLETED("AI analysis completed"),
	PREFLIGHT_COMPLETED("PDF preflight completed"),
	QA_SIGNED_OFF("QA sign-off recorded");

	private final String label;

	NotificationType(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}
