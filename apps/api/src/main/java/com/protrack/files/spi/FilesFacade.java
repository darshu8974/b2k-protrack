package com.protrack.files.spi;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * Published interface of the files module. Lets other modules (packaging, and the ai/preflight
 * modules) resolve a document/version to its stored blob, and upload a production PDF, without
 * depending on the files entities directly.
 */
public interface FilesFacade {

	/** A resolved file reference: the version, its owning document, and its stored location. */
	record FileRef(UUID documentId, UUID versionId, String docType, String title, String fileName,
			String mimeType, long sizeBytes, String storageKey) {
	}

	/**
	 * Store an uploaded production PDF as a new {@code PRODUCTION_PDF} document + version and return
	 * the reference. Validation (PDF-only, size) is enforced by the files module.
	 */
	FileRef uploadProductionPdf(UUID actorId, UUID projectId, MultipartFile file, String title);

	/** Resolve a document's current version, if any. */
	Optional<FileRef> resolveCurrentVersion(UUID documentId);

	/** Resolve a specific version. */
	Optional<FileRef> resolveVersion(UUID versionId);

	/** The current version of every (non-deleted) document in a project — the packaging inputs. */
	List<FileRef> listCurrentFilesForProject(UUID projectId);

	/**
	 * A time-boxed URL an out-of-process consumer (the AI service) can use to fetch a version's blob.
	 * Backed by the storage driver's signing (a {@code file:} URI for local disk).
	 */
	Optional<URI> signedDownloadUrl(UUID versionId, Duration ttl);
}
