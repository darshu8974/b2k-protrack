package com.protrack.shared.storage;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Placeholder S3 {@link StoragePort} for the future object-storage driver
 * ({@code protrack.storage.driver=s3}). Phase 1 ships the local-disk adapter; this class exists so
 * the storage abstraction is provably swappable and the wiring is in place. Implementing the AWS SDK
 * calls (put/get/presign) is a Phase 2 change with no impact on callers.
 */
@Component
@ConditionalOnProperty(name = "protrack.storage.driver", havingValue = "s3")
public class S3StorageAdapter implements StoragePort {

	private static UnsupportedOperationException notImplemented() {
		return new UnsupportedOperationException("S3 storage driver is not implemented in Phase 1.");
	}

	@Override
	public StoredObject store(InputStream content) {
		throw notImplemented();
	}

	@Override
	public Resource load(String storageKey) {
		throw notImplemented();
	}

	@Override
	public boolean exists(String storageKey) {
		throw notImplemented();
	}

	@Override
	public void delete(String storageKey) {
		throw notImplemented();
	}

	@Override
	public URI signedUrl(String storageKey, Duration ttl) {
		throw notImplemented();
	}
}
