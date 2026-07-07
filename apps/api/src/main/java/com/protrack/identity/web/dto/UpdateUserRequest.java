package com.protrack.identity.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Administrator profile/status update (API Specification §3.2). All fields optional — a null field
 * is left unchanged. {@code status} is one of ACTIVE | INACTIVE | SUSPENDED.
 */
public record UpdateUserRequest(
		@Size(max = 160) String fullName,
		@Size(max = 16) String avatarColor,
		@Pattern(regexp = "ACTIVE|INACTIVE|SUSPENDED") String status) {
}
