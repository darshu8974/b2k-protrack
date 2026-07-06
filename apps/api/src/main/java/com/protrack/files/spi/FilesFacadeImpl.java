package com.protrack.files.spi;

import com.protrack.files.domain.DocType;
import com.protrack.files.domain.Document;
import com.protrack.files.domain.FileVersion;
import com.protrack.files.repository.DocumentRepository;
import com.protrack.files.repository.FileVersionRepository;
import com.protrack.files.service.DocumentService;
import com.protrack.shared.storage.StoragePort;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Default {@link FilesFacade} backed by the files repositories and the storage port. */
@Service
public class FilesFacadeImpl implements FilesFacade {

	private final DocumentRepository documentRepository;
	private final FileVersionRepository fileVersionRepository;
	private final StoragePort storagePort;
	private final DocumentService documentService;

	public FilesFacadeImpl(DocumentRepository documentRepository,
			FileVersionRepository fileVersionRepository, StoragePort storagePort,
			DocumentService documentService) {
		this.documentRepository = documentRepository;
		this.fileVersionRepository = fileVersionRepository;
		this.storagePort = storagePort;
		this.documentService = documentService;
	}

	@Override
	public FileRef uploadProductionPdf(UUID actorId, UUID projectId, MultipartFile file,
			String title) {
		FileVersion version =
				documentService.createVersion(actorId, projectId, DocType.PRODUCTION_PDF, title, file);
		Document document = documentRepository.findById(version.getDocumentId()).orElseThrow();
		return toRef(document, version);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<FileRef> resolveCurrentVersion(UUID documentId) {
		return documentRepository.findByIdAndDeletedAtIsNull(documentId)
				.flatMap(document -> fileVersionRepository.findByDocumentIdAndCurrentTrue(documentId)
						.map(version -> toRef(document, version)));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<FileRef> resolveVersion(UUID versionId) {
		return fileVersionRepository.findById(versionId)
				.flatMap(version -> documentRepository.findById(version.getDocumentId())
						.map(document -> toRef(document, version)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<FileRef> listCurrentFilesForProject(UUID projectId) {
		List<Document> documents =
				documentRepository.findByProjectIdAndDeletedAtIsNullOrderByCreatedAtDesc(projectId);
		Map<UUID, Document> byId = documents.stream()
				.collect(Collectors.toMap(Document::getId, document -> document));
		return documents.stream()
				.map(Document::getCurrentVersionId)
				.filter(Objects::nonNull)
				.map(fileVersionRepository::findById)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(version -> toRef(byId.get(version.getDocumentId()), version))
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<URI> signedDownloadUrl(UUID versionId, Duration ttl) {
		return fileVersionRepository.findById(versionId)
				.map(version -> storagePort.signedUrl(version.getStorageKey(), ttl));
	}

	private static FileRef toRef(Document document, FileVersion version) {
		return new FileRef(document.getId(), version.getId(), document.getDocType(),
				document.getTitle(), version.getFileName(), version.getMimeType(),
				version.getSizeBytes(), version.getStorageKey());
	}
}
