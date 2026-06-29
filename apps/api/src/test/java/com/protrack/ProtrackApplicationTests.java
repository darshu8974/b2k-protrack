package com.protrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Smoke test: boots the full application context against a real PostgreSQL (Testcontainers),
 * which also runs the Flyway baseline migration. Verifies wiring, configuration, and migrations
 * with no business logic involved.
 */
@SpringBootTest
@Testcontainers
class ProtrackApplicationTests {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> POSTGRES =
			new PostgreSQLContainer<>("postgres:16-alpine");

	@Test
	void contextLoads() {
		// Context boots + Flyway migrates successfully against the container.
	}
}
