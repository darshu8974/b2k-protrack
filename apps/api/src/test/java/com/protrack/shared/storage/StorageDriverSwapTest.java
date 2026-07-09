package com.protrack.shared.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.protrack.shared.properties.ProtrackProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;

/**
 * Verifies the storage-driver swap path (Sprint-7 hardening: "S3 adapter readiness — swap path
 * verified, not necessarily switched"). The {@code @ConditionalOnProperty} wiring must select the
 * right {@link StoragePort} implementation by {@code protrack.storage.driver}. Pure context-runner
 * test — no database, no Docker.
 */
class StorageDriverSwapTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withUserConfiguration(StorageConfig.class);

	@Test
	void localDriverSelectsTheLocalDiskAdapter() {
		runner.withPropertyValues(
						"protrack.storage.driver=local",
						"protrack.storage.local-path=./build/tmp-storage-test")
				.run(context -> assertThat(context.getBean(StoragePort.class))
						.isInstanceOf(LocalDiskStorageAdapter.class));
	}

	@Test
	void s3DriverSelectsTheS3Adapter() {
		runner.withPropertyValues("protrack.storage.driver=s3")
				.run(context -> assertThat(context.getBean(StoragePort.class))
						.isInstanceOf(S3StorageAdapter.class));
	}

	@Test
	void aMissingDriverDefaultsToLocalDisk() {
		runner.withPropertyValues("protrack.storage.local-path=./build/tmp-storage-test")
				.run(context -> assertThat(context.getBean(StoragePort.class))
						.isInstanceOf(LocalDiskStorageAdapter.class));
	}

	@EnableConfigurationProperties(ProtrackProperties.class)
	@Import({LocalDiskStorageAdapter.class, S3StorageAdapter.class})
	static class StorageConfig {
	}
}
