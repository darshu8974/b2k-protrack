package com.protrack.files.service;

import com.protrack.files.domain.DocType;
import com.protrack.files.domain.Document;
import com.protrack.files.domain.FileVersion;
import com.protrack.files.mapper.DocumentMapper;
import com.protrack.files.repository.DocumentRepository;
import com.protrack.files.repository.FileVersionRepository;
import com.protrack.files.service.UploadService.UploadedBlob;
import com.protrack.files.web.dto.DocumentResponse;
import com.protrack.files.web.dto.FileVersionResponse;
import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.protrack.shared.storage.StoragePort;

/** Version history, new-version upload, rollback (set-current), and download resolution. */
@Service
public class FileVersionService {

	private final DocumentRepository documentRepository;
	private final FileVersionRepository fileVersionRepository;
	private final UploadService uploadService;
	private final ProjectFacade projectFacade;
	private final IdentityFacade identityFacade;
	private final StoragePort storagePort;
	private final DocumentMapper mapper;

	public FileVersionService(DocumentRepository documentRepository,
			FileVersionRepository fileVersionRepository, UploadService uploadService,
			ProjectFacade projectFacade, IdentityFacade identityFacade, StoragePort storagePort,
			DocumentMapper mapper) {
		this.documentRepository = documentRepository;
		this.fileVersionRepository = fileVersionRepository;
		this.uploadService = uploadService;
		this.projectFacade = projectFacade;
		this.identityFacade = identityFacade;
		this.storagePort = storagePort;
		this.mapper = mapper;
	}

	/** A resolved download: the storage resource plus the headers the controller needs. */
	public record Download(Resource resource, String fileName, String mimeType, long sizeBytes) {
	}

	@Transactional(readOnly = true)
	public List<FileVersionResponse> listVersions(UUID documentId) {
		requireDocument(documentId);
		List<FileVersion> versions = fileVersionRepository.findByDocumentIdOrderByVersionNoDesc(documentId);
		Map<UUID, UserBrief> uploaders = identityFacade.findBriefs(versions.stream()
				.map(FileVersion::getUploadedBy).filter(Objects::nonNull).collect(Collectors.toSet()));
		return versions.stream()
				.map(version -> mapper.toVersionResponse(version, nameOf(uploaders, version)))
				.toList();
	}

	/** Upload a new version of an existing document. */
	public FileVersionResponse addVersion(UUID actor, UUID documentId, MultipartFile file) {
		Document document = requireDocument(documentId);
		UUID organizationId = organizationOf(document);

		// Blob first (idempotent by checksum), then a short tx records the version metadata.
		UploadedBlob blob = uploadService.store(file, docTypeOf(document));
		FileVersion version = uploadService.addVersion(organizationId, document, blob, actor);
		return mapper.toVersionResponse(version, uploaderName(version));
	}

	/** Roll back / promote a given version to be the document's current version. */
	@Transactional
	public DocumentResponse setCurrent(UUID actor, UUID documentId, UUID versionId) {
		Document document = requireDocument(documentId);
		FileVersion version = fileVersionRepository.findById(versionId)
				.orElseThrow(() -> new NotFoundException("File version not found."));
		if (!version.getDocumentId().equals(documentId)) {
			throw new NotFoundException("File version not found.");
		}

		if (!version.isCurrent()) {
			fileVersionRepository.clearCurrentForDocument(documentId);
			fileVersionRepository.flush();
			version.setCurrent(true);
			fileVersionRepository.save(version);
		}
		document.setCurrentVersionId(versionId);
		document.setUpdatedBy(actor);
		documentRepository.save(document);

		return mapper.toResponse(document, version, uploaderName(version));
	}

	@Transactional(readOnly = true)
	public Download download(UUID versionId) {
		FileVersion version = fileVersionRepository.findById(versionId)
				.orElseThrow(() -> new NotFoundException("File version not found."));
		Resource resource = storagePort.load(version.getStorageKey());
		return new Download(resource, version.getFileName(), version.getMimeType(), version.getSizeBytes());
	}

	// --- helpers ---

	private Document requireDocument(UUID documentId) {
		return documentRepository.findByIdAndDeletedAtIsNull(documentId)
				.orElseThrow(() -> new NotFoundException("Document not found."));
	}

	private UUID organizationOf(Document document) {
		return projectFacade.findStageInfo(document.getProjectId())
				.map(ProjectFacade.ProjectStageInfo::organizationId)
				.orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "PROJECT_MISSING",
						"The document's project no longer exists."));
	}

	private static DocType docTypeOf(Document document) {
		try {
			return DocType.valueOf(document.getDocType());
		} catch (IllegalArgumentException ex) {
			// Unknown/free-form type stored in the DB — treat as unrestricted for validation.
			return DocType.OTHER;
		}
	}

	private String uploaderName(FileVersion version) {
		if (version == null || version.getUploadedBy() == null) {
			return null;
		}
		return identityFacade.findBrief(version.getUploadedBy()).map(UserBrief::fullName).orElse(null);
	}

	private static String nameOf(Map<UUID, UserBrief> uploaders, FileVersion version) {
		if (version.getUploadedBy() == null) {
			return null;
		}
		UserBrief brief = uploaders.get(version.getUploadedBy());
		return brief == null ? null : brief.fullName();
	}
}
