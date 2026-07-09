package com.protrack.shared.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.protrack.shared.properties.ProtrackProperties;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

/** Unit tests for {@link StorageHealthIndicator} (no Docker). */
class StorageHealthIndicatorTest {

	@TempDir
	Path tempDir;

	private static ProtrackProperties props(String driver, String path) {
		return new ProtrackProperties(null, new ProtrackProperties.Storage(driver, path), null, null);
	}

	@Test
	void localDriverWithAWritableRootIsUp() {
		Health health = new StorageHealthIndicator(props("local", tempDir.toString())).health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsEntry("driver", "local");
		assertThat(health.getDetails()).containsKey("path");
	}

	@Test
	void localDriverCreatesAMissingRootAndReportsUp() {
		Path missing = tempDir.resolve("does-not-exist-yet");
		Health health = new StorageHealthIndicator(props("local", missing.toString())).health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
	}

	@Test
	void nonLocalDriverReportsItsConfiguredDriverWithoutProbing() {
		Health health = new StorageHealthIndicator(props("s3", null)).health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsEntry("driver", "s3");
	}
}
