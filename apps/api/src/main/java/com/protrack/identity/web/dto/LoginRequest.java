package com.protrack.identity.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credentials for POST /api/v1/auth/login. */
public record LoginRequest(
		@NotBlank @Email String email,
		@NotBlank String password) {
}
