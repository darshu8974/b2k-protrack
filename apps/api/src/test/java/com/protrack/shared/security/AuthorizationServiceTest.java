package com.protrack.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

/** Unit tests for {@link AuthorizationService} (no Spring context, no Docker). */
class AuthorizationServiceTest {

	private final AuthorizationService authz = new AuthorizationService();

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	private void authenticateWith(String... authorities) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(
						"user-1", null, AuthorityUtils.createAuthorityList(authorities)));
	}

	@Test
	void evaluatesRolesAndPermissions() {
		authenticateWith("ROLE_ADMIN", "PROJECT_CREATE");

		assertThat(authz.hasAnyRole("ADMIN")).isTrue();
		assertThat(authz.hasAnyRole("PM", "QA")).isFalse();
		assertThat(authz.hasPermission("PROJECT_CREATE")).isTrue();
		assertThat(authz.hasPermission("OTHER")).isFalse();
	}

	@Test
	void deniesWhenUnauthenticated() {
		assertThat(authz.hasAnyRole("ADMIN")).isFalse();
		assertThat(authz.hasPermission("PROJECT_CREATE")).isFalse();
	}
}
