package com.protrack.identity.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Administrator user creation (API Specification §3.2). Creates the account with one initial role. */
public record CreateUserRequest(
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(max = 160) String fullName,
		@NotNull Integer roleId,
		@NotBlank @Size(min = 8, max = 100) String password,
		@Size(max = 16) String avatarColor) {
}
