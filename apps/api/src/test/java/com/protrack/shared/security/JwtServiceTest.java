package com.protrack.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.protrack.shared.properties.ProtrackProperties;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link JwtService} (no Spring context, no Docker). */
class JwtServiceTest {

	private final JwtService jwtService = new JwtService(new ProtrackProperties(
			new ProtrackProperties.Jwt(
					"unit-test-secret-key-at-least-32-bytes-long",
					Duration.ofMinutes(15),
					Duration.ofDays(7)),
			null, null, null));

	@Test
	void generatesAndParsesAccessToken() {
		String token = jwtService.generateAccessToken(
				"user-1", "user@protrack.io", List.of("ADMIN"), List.of("PROJECT_CREATE"));

		Claims claims = jwtService.parse(token);

		assertThat(claims.getSubject()).isEqualTo("user-1");
		assertThat(claims.get("email", String.class)).isEqualTo("user@protrack.io");
		assertThat(claims.get("roles", List.class)).containsExactly("ADMIN");
		assertThat(claims.get("permissions", List.class)).containsExactly("PROJECT_CREATE");
		assertThat(jwtService.getAccessTtlSeconds()).isEqualTo(900);
	}
}
