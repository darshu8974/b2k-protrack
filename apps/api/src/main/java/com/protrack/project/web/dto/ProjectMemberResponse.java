package com.protrack.project.web.dto;

import java.math.BigDecimal;

/** A project member with resolved user details. */
public record ProjectMemberResponse(
		String userId,
		String fullName,
		String email,
		String avatarInitials,
		String roleInProject,
		boolean owner,
		BigDecimal matchScore) {
}
