package com.protrack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Smoke test: boots the full application context against a real PostgreSQL (Testcontainers),
 * which also runs the Flyway migrations. Verifies wiring, configuration, and migrations with no
 * business logic involved.
 *
 * <p>Requires a Docker daemon; skipped (not failed) where Docker is unavailable, so the suite stays
 * green on dev machines without Docker. CI runs it.
 */
@SpringBootTest
@Testcontainers
@EnabledIf("dockerAvailable")
class ProtrackApplicationTests {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> POSTGRES =
			new PostgreSQLContainer<>("postgres:16-alpine");

	@Test
	void contextLoads() {
		// Context boots + Flyway migrates successfully against the container.
	}

	@SuppressWarnings("unused")
	static boolean dockerAvailable() {
		try {
			return DockerClientFactory.instance().isDockerAvailable();
		} catch (Throwable ex) {
			return false;
		}
	}
}
