package com.protrack.identity.web.dto;

import java.util.List;

/** Authenticated user details returned by login and GET /api/v1/auth/me. */
public record UserSummary(
		String id,
		String email,
		String fullName,
		String avatarInitials,
		String avatarColor,
		List<String> roles,
		List<String> permissions) {
}
