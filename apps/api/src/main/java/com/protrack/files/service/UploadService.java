package com.protrack.files.service;

import com.protrack.files.domain.DocType;
import com.protrack.files.domain.Document;
import com.protrack.files.domain.FileVersion;
import com.protrack.files.repository.DocumentRepository;
import com.protrack.files.repository.FileVersionRepository;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.events.FileEvents;
import com.protrack.shared.storage.StoragePort;
import com.protrack.shared.storage.StoragePort.StoredObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Upload mechanics shared by the files module: type/size validation, content-addressed storage, and
 * immutable version persistence with the {@code is_current} flip.
 *
 * <p>Per the backend architecture, the binary is written to storage <em>first</em> (idempotent by
 * checksum), then a short transaction records the {@code file_version} metadata — so a failed
 * metadata write never orphans anything critical and a failed upload writes no metadata.
 */
@Service
public class UploadService {

	/** Phase 1 per-file cap enforced at the service layer (well under the multipart container cap). */
	private static final long MAX_FILE_BYTES = 100L * 1024 * 1024;

	private static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	private static final String PDF = "application/pdf";

	private final DocumentRepository documentRepository;
	private final FileVersionRepository fileVersionRepository;
	private final StoragePort storagePort;
	private final ApplicationEventPublisher eventPublisher;

	public UploadService(DocumentRepository documentRepository,
			FileVersionRepository fileVersionRepository, StoragePort storagePort,
			ApplicationEventPublisher eventPublisher) {
		this.documentRepository = documentRepository;
		this.fileVersionRepository = fileVersionRepository;
		this.storagePort = storagePort;
		this.eventPublisher = eventPublisher;
	}

	/** Validated blob written to storage, plus the original file's display metadata. */
	public record UploadedBlob(String storageKey, String checksumSha256, long sizeBytes,
			String fileName, String mimeType) {
	}

	/** Validate the upload against the document type's rules and stream it into storage (no DB tx). */
	public UploadedBlob store(MultipartFile file, DocType docType) {
		validate(file, docType);
		String fileName = StringUtils.hasText(file.getOriginalFilename())
				? StringUtils.cleanPath(file.getOriginalFilename()) : "upload";
		String mimeType = StringUtils.hasText(file.getContentType())
				? file.getContentType() : "application/octet-stream";
		try (InputStream in = file.getInputStream()) {
			StoredObject stored = storagePort.store(in);
			return new UploadedBlob(stored.storageKey(), stored.checksumSha256(), stored.sizeBytes(),
					fileName, mimeType);
		} catch (IOException ex) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR",
					"Failed to read the uploaded file.");
		}
	}

	/** Create a new logical document and its first (current) version. */
	@Transactional
	public FileVersion createDocumentWithVersion(UUID projectId, UUID organizationId, DocType docType,
			String title, UploadedBlob blob, UUID actor) {
		Document document = new Document(UUID.randomUUID(), projectId, docType, title, actor);
		documentRepository.save(document);

		FileVersion version = persistVersion(document, 1, blob, actor);
		document.setCurrentVersionId(version.getId());
		document.setUpdatedBy(actor);
		documentRepository.save(document);

		publishUploaded(organizationId, projectId, actor, document, version);
		return version;
	}

	/** Append a new (current) version to an existing document, demoting the previous current one. */
	@Transactional
	public FileVersion addVersion(UUID organizationId, Document document, UploadedBlob blob, UUID actor) {
		int nextNo = fileVersionRepository.findFirstByDocumentIdOrderByVersionNoDesc(document.getId())
				.map(FileVersion::getVersionNo).orElse(0) + 1;

		// Demote the existing current version before inserting the new one (partial-unique index).
		fileVersionRepository.clearCurrentForDocument(document.getId());
		fileVersionRepository.flush();

		FileVersion version = persistVersion(document, nextNo, blob, actor);
		document.setCurrentVersionId(version.getId());
		document.setUpdatedBy(actor);
		documentRepository.save(document);

		publishUploaded(organizationId, document.getProjectId(), actor, document, version);
		return version;
	}

	private FileVersion persistVersion(Document document, int versionNo, UploadedBlob blob, UUID actor) {
		FileVersion version = new FileVersion(UUID.randomUUID(), document.getId(), versionNo,
				blob.fileName(), blob.mimeType(), blob.sizeBytes(), blob.storageKey(),
				blob.checksumSha256(), true, actor);
		return fileVersionRepository.saveAndFlush(version);
	}

	private void publishUploaded(UUID organizationId, UUID projectId, UUID actor, Document document,
			FileVersion version) {
		eventPublisher.publishEvent(new FileEvents.FileUploaded(organizationId, projectId, actor,
				document.getId(), version.getId(), document.getDocType(), version.getFileName(),
				version.getVersionNo()));
	}

	// --- validation ---

	private void validate(MultipartFile file, DocType docType) {
		if (file == null || file.isEmpty()) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "EMPTY_FILE",
					"The uploaded file is empty.");
		}
		if (file.getSize() > MAX_FILE_BYTES) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "FILE_TOO_LARGE",
					"The uploaded file exceeds the maximum allowed size.");
		}
		Set<String> allowedMimes = allowedMimes(docType);
		if (allowedMimes.isEmpty()) {
			return; // Unrestricted type (e.g. OTHER).
		}
		Set<String> allowedExtensions = allowedExtensions(docType);
		String extension = extensionOf(file.getOriginalFilename());
		String mime = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
		boolean ok = allowedExtensions.contains(extension) || allowedMimes.contains(mime);
		if (!ok) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "UNSUPPORTED_FILE_TYPE",
					"File type not accepted for %s. Allowed: %s."
							.formatted(docType.name(), String.join(", ", allowedExtensions)));
		}
	}

	private static Set<String> allowedMimes(DocType docType) {
		return switch (docType) {
			case MANUSCRIPT -> Set.of(DOCX, PDF);
			case PRODUCTION_PDF -> Set.of(PDF);
			case STRUCTURED_XML -> Set.of("application/xml", "text/xml");
			case FIGURES_MANIFEST -> Set.of("application/json", "text/csv", "application/zip",
					"application/x-zip-compressed");
			case OTHER -> Set.of();
		};
	}

	private static Set<String> allowedExtensions(DocType docType) {
		return switch (docType) {
			case MANUSCRIPT -> Set.of("docx", "pdf");
			case PRODUCTION_PDF -> Set.of("pdf");
			case STRUCTURED_XML -> Set.of("xml");
			case FIGURES_MANIFEST -> Set.of("json", "csv", "zip");
			case OTHER -> Set.of();
		};
	}

	private static String extensionOf(String filename) {
		if (!StringUtils.hasText(filename)) {
			return "";
		}
		String ext = StringUtils.getFilenameExtension(filename);
		return ext == null ? "" : ext.toLowerCase(Locale.ROOT);
	}
}
