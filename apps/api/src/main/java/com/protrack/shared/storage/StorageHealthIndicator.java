package com.protrack.shared.storage;

import com.protrack.shared.properties.ProtrackProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Actuator health component ("storage") reporting the active storage driver. For the local-disk
 * driver it verifies the blob root is a writable directory (creating it if missing); other drivers
 * (S3, Phase 2) report their configured state — a live bucket probe is a Phase-2 addition. Storage
 * is a core dependency and is included in the readiness probe group (see {@code application.yml}).
 */
@Component
public class StorageHealthIndicator implements HealthIndicator {

	private final String driver;
	private final String localPath;

	public StorageHealthIndicator(ProtrackProperties properties) {
		this.driver = properties.storage().driver();
		this.localPath = properties.storage().localPath();
	}

	@Override
	public Health health() {
		if (!"local".equalsIgnoreCase(driver)) {
			return Health.up().withDetail("driver", driver).build();
		}
		try {
			Path root = Path.of(localPath).toAbsolutePath().normalize();
			Files.createDirectories(root);
			boolean writable = Files.isDirectory(root) && Files.isWritable(root);
			return (writable ? Health.up() : Health.down())
					.withDetail("driver", driver)
					.withDetail("path", root.toString())
					.build();
		} catch (Exception ex) {
			return Health.down().withDetail("driver", driver)
					.withDetail("error", ex.getClass().getSimpleName()).build();
		}
	}
}
