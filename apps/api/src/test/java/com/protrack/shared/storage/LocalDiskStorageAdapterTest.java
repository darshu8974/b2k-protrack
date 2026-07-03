package com.protrack.shared.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.protrack.shared.error.ApiException;
import com.protrack.shared.properties.ProtrackProperties;
import com.protrack.shared.storage.StoragePort.StoredObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link LocalDiskStorageAdapter} against a real temp directory (no Spring, no Docker).
 * Covers content-addressed checksums, checksum dedupe, round-trip load, delete, and traversal guard.
 */
class LocalDiskStorageAdapterTest {

	// Known SHA-256 of the ASCII bytes "hello".
	private static final String HELLO_SHA256 =
			"2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";

	@TempDir
	Path root;

	private LocalDiskStorageAdapter adapter() {
		ProtrackProperties props = new ProtrackProperties(
				null, new ProtrackProperties.Storage("local", root.toString()), null, null);
		return new LocalDiskStorageAdapter(props);
	}

	private static InputStream bytes(String content) {
		return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void storeComputesSha256AndContentAddressedKey() {
		StoredObject stored = adapter().store(bytes("hello"));

		assertThat(stored.checksumSha256()).isEqualTo(HELLO_SHA256);
		assertThat(stored.sizeBytes()).isEqualTo(5);
		assertThat(stored.storageKey()).isEqualTo("blobs/2c/" + HELLO_SHA256);
	}

	@Test
	void identicalContentDeduplicatesToOneBlob() throws IOException {
		LocalDiskStorageAdapter adapter = adapter();
		StoredObject first = adapter.store(bytes("duplicate me"));
		StoredObject second = adapter.store(bytes("duplicate me"));

		assertThat(second.storageKey()).isEqualTo(first.storageKey());
		try (Stream<Path> files = Files.walk(root.resolve("blobs"))) {
			long blobCount = files.filter(Files::isRegularFile).count();
			assertThat(blobCount).isEqualTo(1);
		}
	}

	@Test
	void differentContentProducesDifferentKeys() {
		LocalDiskStorageAdapter adapter = adapter();
		assertThat(adapter.store(bytes("alpha")).storageKey())
				.isNotEqualTo(adapter.store(bytes("beta")).storageKey());
	}

	@Test
	void loadReturnsStoredBytes() throws IOException {
		LocalDiskStorageAdapter adapter = adapter();
		StoredObject stored = adapter.store(bytes("round trip"));

		byte[] loaded = adapter.load(stored.storageKey()).getInputStream().readAllBytes();
		assertThat(new String(loaded, StandardCharsets.UTF_8)).isEqualTo("round trip");
	}

	@Test
	void existsAndDeleteBehaveConsistently() {
		LocalDiskStorageAdapter adapter = adapter();
		StoredObject stored = adapter.store(bytes("temporary"));

		assertThat(adapter.exists(stored.storageKey())).isTrue();
		adapter.delete(stored.storageKey());
		assertThat(adapter.exists(stored.storageKey())).isFalse();
	}

	@Test
	void loadingMissingBlobThrowsNotFound() {
		assertThatThrownBy(() -> adapter().load("blobs/00/" + "0".repeat(64)))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("FILE_MISSING"));
	}

	@Test
	void rejectsKeysThatEscapeTheStorageRoot() {
		assertThatThrownBy(() -> adapter().exists("../escape"))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("INVALID_STORAGE_KEY"));
	}
}
