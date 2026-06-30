package com.protrack.identity.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Body for POST /api/v1/auth/refresh and POST /api/v1/auth/logout. */
public record RefreshTokenRequest(
		@NotBlank String refreshToken) {
}
