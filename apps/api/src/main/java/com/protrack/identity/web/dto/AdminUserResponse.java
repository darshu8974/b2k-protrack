package com.protrack.identity.web.dto;

import com.protrack.identity.domain.Role;
import com.protrack.identity.domain.User;
import java.time.Instant;
import java.util.List;

/** A user directory row returned to administrators (API Specification §3.2). */
public record AdminUserResponse(
		String id,
		String email,
		String fullName,
		String avatarInitials,
		String avatarColor,
		String status,
		List<String> roles,
		Instant lastLoginAt) {

	public static AdminUserResponse from(User user) {
		return new AdminUserResponse(
				user.getId().toString(),
				user.getEmail(),
				user.getFullName(),
				user.getAvatarInitials(),
				user.getAvatarColor(),
				user.getStatus(),
				user.getRoles().stream().map(Role::getCode).sorted().toList(),
				user.getLastLoginAt());
	}
}
