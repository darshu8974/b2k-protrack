package com.protrack.packaging.service;

import com.protrack.files.spi.FilesFacade;
import com.protrack.files.spi.FilesFacade.FileRef;
import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.packaging.domain.PackageItem;
import com.protrack.packaging.domain.ProductionPackage;
import com.protrack.packaging.mapper.PackageMapper;
import com.protrack.packaging.repository.PackageRepository;
import com.protrack.packaging.web.dto.AddPackageItemRequest;
import com.protrack.packaging.web.dto.PackageResponse;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectStageInfo;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.events.PackageEvents;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Assembles and curates the production package: rebuilds the contents from the project's current
 * document versions (no IDML generation — references only), and supports manual add/remove of items.
 */
@Service
public class PackageAssemblyService {

	private final PackageRepository packageRepository;
	private final FilesFacade filesFacade;
	private final ProjectFacade projectFacade;
	private final IdentityFacade identityFacade;
	private final PackageMapper mapper;
	private final ApplicationEventPublisher eventPublisher;

	public PackageAssemblyService(PackageRepository packageRepository, FilesFacade filesFacade,
			ProjectFacade projectFacade, IdentityFacade identityFacade, PackageMapper mapper,
			ApplicationEventPublisher eventPublisher) {
		this.packageRepository = packageRepository;
		this.filesFacade = filesFacade;
		this.projectFacade = projectFacade;
		this.identityFacade = identityFacade;
		this.mapper = mapper;
		this.eventPublisher = eventPublisher;
	}

	@Transactional(readOnly = true)
	public PackageResponse get(UUID actor, UUID projectId) {
		requireProjectInOrg(actor, projectId);
		ProductionPackage pkg = packageRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
				.orElseThrow(() -> new NotFoundException("No production package for this project yet."));
		return toResponse(pkg);
	}

	/** Assemble (or re-assemble) the package from the project's current document versions. */
	@Transactional
	public PackageResponse assemble(UUID actor, UUID projectId) {
		UUID organizationId = requireProjectInOrg(actor, projectId).organizationId();

		ProductionPackage pkg = packageRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
				.orElseGet(() -> new ProductionPackage(UUID.randomUUID(), projectId));

		List<FileRef> files = filesFacade.listCurrentFilesForProject(projectId);
		List<PackageItem> items = new ArrayList<>();
		int order = 0;
		for (FileRef file : files) {
			items.add(new PackageItem(UUID.randomUUID(), pkg, file.documentId(), file.docType(),
					labelFor(file), file.sizeBytes(), order++));
		}
		pkg.replaceItems(items);
		pkg.markAssembled(actor, Instant.now());

		ProductionPackage saved = packageRepository.save(pkg);
		eventPublisher.publishEvent(new PackageEvents.PackageAssembled(organizationId, projectId, actor,
				saved.getId(), saved.getItemCount(), saved.getTotalSizeBytes()));
		return toResponse(saved);
	}

	/** Manually add a document as a package item (curation after assembly). */
	@Transactional
	public PackageResponse addItem(UUID actor, UUID projectId, AddPackageItemRequest request) {
		requireProjectInOrg(actor, projectId);
		ProductionPackage pkg = requirePackage(projectId);

		FileRef file = filesFacade.resolveCurrentVersion(request.documentId())
				.orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ITEM",
						"Document has no current version to include."));
		if (!file.documentId().equals(request.documentId())) {
			throw new NotFoundException("Document not found.");
		}

		int sortOrder = request.sortOrder() != null ? request.sortOrder() : pkg.getItems().size();
		String itemType = StringUtils.hasText(request.itemType()) ? request.itemType() : file.docType();
		String label = StringUtils.hasText(request.label()) ? request.label() : labelFor(file);
		pkg.addItem(new PackageItem(UUID.randomUUID(), pkg, file.documentId(), itemType, label,
				file.sizeBytes(), sortOrder));

		return toResponse(packageRepository.save(pkg));
	}

	/** Remove an item from the package. */
	@Transactional
	public void removeItem(UUID actor, UUID projectId, UUID itemId) {
		requireProjectInOrg(actor, projectId);
		ProductionPackage pkg = requirePackage(projectId);
		PackageItem item = pkg.getItems().stream()
				.filter(candidate -> candidate.getId().equals(itemId)).findFirst()
				.orElseThrow(() -> new NotFoundException("Package item not found."));
		pkg.removeItem(item);
		packageRepository.save(pkg);
	}

	// --- helpers ---

	private ProductionPackage requirePackage(UUID projectId) {
		return packageRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
				.orElseThrow(() -> new NotFoundException("No production package for this project yet."));
	}

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

	private PackageResponse toResponse(ProductionPackage pkg) {
		String assembledByName = pkg.getAssembledBy() == null ? null
				: identityFacade.findBrief(pkg.getAssembledBy()).map(UserBrief::fullName).orElse(null);
		return mapper.toResponse(pkg, assembledByName);
	}

	private static String labelFor(FileRef file) {
		return StringUtils.hasText(file.title()) ? file.title() : file.fileName();
	}
}
