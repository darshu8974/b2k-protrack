package com.protrack.identity.web.dto;

/**
 * Successful authentication response. Refresh tokens are intentionally absent in this task and are
 * added in a later task.
 */
public record TokenResponse(
		String accessToken,
		String tokenType,
		long expiresIn,
		UserSummary user) {
}
