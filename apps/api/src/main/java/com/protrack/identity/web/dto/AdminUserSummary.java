package com.protrack.identity.web.dto;

import java.util.List;

/** User directory row returned to administrators by GET /api/v1/admin/users. */
public record AdminUserSummary(
		String id,
		String email,
		String fullName,
		String status,
		List<String> roles) {
}
