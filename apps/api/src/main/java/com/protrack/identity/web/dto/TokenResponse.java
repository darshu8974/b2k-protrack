package com.protrack.identity.web.dto;

/** Successful authentication response: a short-lived access token plus a rotating refresh token. */
public record TokenResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresIn,
		UserSummary user) {
}
