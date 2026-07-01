package com.protrack.shared.storage;

import com.protrack.shared.error.ApiException;
import com.protrack.shared.properties.ProtrackProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Local-disk {@link StoragePort} (Phase 1 default, {@code protrack.storage.driver=local}).
 *
 * <p>Content-addressed layout: {@code <root>/blobs/<aa>/<sha256>} where {@code aa} is the checksum
 * prefix (fans out directories). Because the key is the checksum, identical uploads deduplicate to a
 * single blob. The checksum is computed while streaming, so large files never sit fully in memory.
 */
@Component
@ConditionalOnProperty(name = "protrack.storage.driver", havingValue = "local", matchIfMissing = true)
public class LocalDiskStorageAdapter implements StoragePort {

	private static final String BLOBS_DIR = "blobs";
	private static final String TEMP_DIR = ".tmp";

	private final Path root;

	public LocalDiskStorageAdapter(ProtrackProperties properties) {
		this.root = Path.of(properties.storage().localPath()).toAbsolutePath().normalize();
	}

	@Override
	public StoredObject store(InputStream content) {
		try {
			Path tempDir = root.resolve(TEMP_DIR);
			Files.createDirectories(tempDir);
			Path temp = Files.createTempFile(tempDir, "upload-", ".part");

			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			long size;
			try (OutputStream fileOut = Files.newOutputStream(temp);
					DigestOutputStream out = new DigestOutputStream(fileOut, digest)) {
				size = content.transferTo(out);
			}

			String checksum = toHex(digest.digest());
			Path finalPath = blobPath(checksum);
			Files.createDirectories(finalPath.getParent());
			if (Files.exists(finalPath)) {
				// Identical content already stored — dedupe: drop the temp copy.
				Files.deleteIfExists(temp);
			} else {
				Files.move(temp, finalPath, StandardCopyOption.ATOMIC_MOVE);
			}
			return new StoredObject(relativeKey(checksum), checksum, size);
		} catch (IOException ex) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR",
					"Failed to store file.");
		} catch (NoSuchAlgorithmException ex) {
			// SHA-256 is guaranteed by the JDK; treat as fatal misconfiguration.
			throw new IllegalStateException("SHA-256 unavailable", ex);
		}
	}

	@Override
	public Resource load(String storageKey) {
		Path path = resolve(storageKey);
		if (!Files.exists(path)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "FILE_MISSING", "Stored file not found.");
		}
		return new FileSystemResource(path);
	}

	@Override
	public boolean exists(String storageKey) {
		return Files.exists(resolve(storageKey));
	}

	@Override
	public void delete(String storageKey) {
		try {
			Files.deleteIfExists(resolve(storageKey));
		} catch (IOException ex) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR",
					"Failed to delete file.");
		}
	}

	@Override
	public URI signedUrl(String storageKey, Duration ttl) {
		// Local driver has no real signing; the file URI is sufficient for co-located consumers.
		// The S3 adapter (future) returns a genuine time-boxed presigned URL.
		return resolve(storageKey).toUri();
	}

	// --- helpers ---

	private Path blobPath(String checksum) {
		return root.resolve(BLOBS_DIR).resolve(checksum.substring(0, 2)).resolve(checksum);
	}

	private String relativeKey(String checksum) {
		return BLOBS_DIR + "/" + checksum.substring(0, 2) + "/" + checksum;
	}

	/** Resolve a key under the root, rejecting traversal outside the storage root. */
	private Path resolve(String storageKey) {
		Path path = root.resolve(storageKey).normalize();
		if (!path.startsWith(root)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STORAGE_KEY", "Invalid storage key.");
		}
		return path;
	}

	private static String toHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			sb.append(Character.forDigit((b >> 4) & 0xF, 16));
			sb.append(Character.forDigit(b & 0xF, 16));
		}
		return sb.toString();
	}
}
