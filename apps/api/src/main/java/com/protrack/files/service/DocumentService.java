package com.protrack.files.service;

import com.protrack.files.domain.DocType;
import com.protrack.files.domain.Document;
import com.protrack.files.domain.FileVersion;
import com.protrack.files.mapper.DocumentMapper;
import com.protrack.files.repository.DocumentRepository;
import com.protrack.files.repository.FileVersionRepository;
import com.protrack.files.service.UploadService.UploadedBlob;
import com.protrack.files.web.dto.DocumentResponse;
import com.protrack.files.web.dto.DocumentSummaryResponse;
import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectStageInfo;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** Document lifecycle: create a logical document with its first version, read, and list. */
@Service
public class DocumentService {

	private final DocumentRepository documentRepository;
	private final FileVersionRepository fileVersionRepository;
	private final UploadService uploadService;
	private final ProjectFacade projectFacade;
	private final IdentityFacade identityFacade;
	private final DocumentMapper mapper;

	public DocumentService(DocumentRepository documentRepository,
			FileVersionRepository fileVersionRepository, UploadService uploadService,
			ProjectFacade projectFacade, IdentityFacade identityFacade, DocumentMapper mapper) {
		this.documentRepository = documentRepository;
		this.fileVersionRepository = fileVersionRepository;
		this.uploadService = uploadService;
		this.projectFacade = projectFacade;
		this.identityFacade = identityFacade;
		this.mapper = mapper;
	}

	/** Create a new document and upload its first version. */
	public DocumentResponse create(UUID actor, UUID projectId, DocType docType, String title,
			MultipartFile file) {
		FileVersion version = createVersion(actor, projectId, docType, title, file);
		Document document = documentRepository.findById(version.getDocumentId()).orElseThrow();
		return mapper.toResponse(document, version, uploaderName(version));
	}

	/** Create a new document with its first version, returning the version (for facade callers). */
	public FileVersion createVersion(UUID actor, UUID projectId, DocType docType, String title,
			MultipartFile file) {
		UUID organizationId = requireProjectInOrg(actor, projectId).organizationId();
		String resolvedTitle = StringUtils.hasText(title) ? title.trim() : originalName(file);

		// Blob first (idempotent by checksum), then a short tx records the version metadata.
		UploadedBlob blob = uploadService.store(file, docType);
		return uploadService.createDocumentWithVersion(
				projectId, organizationId, docType, resolvedTitle, blob, actor);
	}

	/** Convenience: upload a manuscript (DOCX/PDF) as a new document. */
	public DocumentResponse uploadManuscript(UUID actor, UUID projectId, String title,
			MultipartFile file) {
		return create(actor, projectId, DocType.MANUSCRIPT, title, file);
	}

	@Transactional(readOnly = true)
	public DocumentResponse get(UUID documentId) {
		Document document = documentRepository.findByIdAndDeletedAtIsNull(documentId)
				.orElseThrow(() -> new NotFoundException("Document not found."));
		FileVersion current = currentVersion(document);
		return mapper.toResponse(document, current, uploaderName(current));
	}

	@Transactional(readOnly = true)
	public List<DocumentSummaryResponse> list(UUID actor, UUID projectId, String docType) {
		requireProjectInOrg(actor, projectId);

		List<Document> documents = StringUtils.hasText(docType)
				? documentRepository.findByProjectIdAndDocTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
						projectId, docType.trim().toUpperCase())
				: documentRepository.findByProjectIdAndDeletedAtIsNullOrderByCreatedAtDesc(projectId);
		if (documents.isEmpty()) {
			return List.of();
		}

		List<UUID> documentIds = documents.stream().map(Document::getId).toList();
		Map<UUID, List<FileVersion>> versionsByDocument =
				fileVersionRepository.findByDocumentIdInOrderByVersionNoDesc(documentIds).stream()
						.collect(Collectors.groupingBy(FileVersion::getDocumentId));

		Map<UUID, UserBrief> uploaders = resolveUploaders(
				versionsByDocument.values().stream().flatMap(List::stream));

		return documents.stream().map(document -> {
			List<FileVersion> versions = versionsByDocument.getOrDefault(document.getId(), List.of());
			FileVersion current = versions.stream().filter(FileVersion::isCurrent).findFirst()
					.orElseGet(() -> versions.stream()
							.max(Comparator.comparingInt(FileVersion::getVersionNo)).orElse(null));
			return mapper.toSummary(document, current, versions.size(), nameOf(uploaders, current));
		}).toList();
	}

	// --- helpers ---

	private ProjectStageInfo requireProjectInOrg(UUID actor, UUID projectId) {
		UUID organizationId = identityFacade.findOrganizationId(actor)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
						"Authenticated user no longer exists."));
		ProjectStageInfo info = projectFacade.findStageInfo(projectId)
				.orElseThrow(() -> new NotFoundException("Project not found."));
		if (!organizationId.equals(info.organizationId())) {
			throw new NotFoundException("Project not found.");
		}
		return info;
	}

	private FileVersion currentVersion(Document document) {
		if (document.getCurrentVersionId() == null) {
			return null;
		}
		return fileVersionRepository.findById(document.getCurrentVersionId()).orElse(null);
	}

	private String uploaderName(FileVersion version) {
		if (version == null || version.getUploadedBy() == null) {
			return null;
		}
		return identityFacade.findBrief(version.getUploadedBy()).map(UserBrief::fullName).orElse(null);
	}

	private Map<UUID, UserBrief> resolveUploaders(java.util.stream.Stream<FileVersion> versions) {
		Set<UUID> ids = versions.map(FileVersion::getUploadedBy).filter(Objects::nonNull)
				.collect(Collectors.toSet());
		return identityFacade.findBriefs(ids);
	}

	private static String nameOf(Map<UUID, UserBrief> uploaders, FileVersion version) {
		if (version == null || version.getUploadedBy() == null) {
			return null;
		}
		UserBrief brief = uploaders.get(version.getUploadedBy());
		return brief == null ? null : brief.fullName();
	}

	private static String originalName(MultipartFile file) {
		return Optional.ofNullable(file.getOriginalFilename())
				.filter(StringUtils::hasText).map(StringUtils::cleanPath).orElse("Untitled");
	}
}
