package com.protrack.shared.storage;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import org.springframework.core.io.Resource;

/**
 * Outbound port for binary object storage. The application persists only file <em>metadata</em>
 * (in {@code file_versions}); the bytes live behind this port. Phase 1 uses a local-disk adapter;
 * an S3 adapter slots in without touching callers (see {@link S3StorageAdapter}).
 *
 * <p>Storage is content-addressed and idempotent by checksum: storing identical bytes twice yields
 * the same {@code storageKey} and writes the blob once (checksum dedupe).
 */
public interface StoragePort {

	/** Result of a store operation: the content-addressed key plus integrity metadata. */
	record StoredObject(String storageKey, String checksumSha256, long sizeBytes) {
	}

	/**
	 * Persist a stream of bytes and return its content-addressed key and SHA-256 checksum.
	 * The stream is consumed but not closed by this method.
	 */
	StoredObject store(InputStream content);

	/** Open a stored object for reading (streaming download). */
	Resource load(String storageKey);

	/** True if a blob exists for the given key. */
	boolean exists(String storageKey);

	/** Remove a stored blob if present (metadata rows are the system of record, not the blob). */
	void delete(String storageKey);

	/**
	 * A time-boxed URL an out-of-process consumer (the AI service) can use to fetch the blob.
	 * For the local-disk driver this is a {@code file:} URI; the S3 driver returns a presigned URL.
	 */
	URI signedUrl(String storageKey, Duration ttl);
}
