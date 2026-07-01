package com.protrack.packaging.service;

import com.protrack.files.spi.FilesFacade;
import com.protrack.files.spi.FilesFacade.FileRef;
import com.protrack.identity.spi.IdentityFacade;
import com.protrack.packaging.domain.PackageItem;
import com.protrack.packaging.domain.ProductionPackage;
import com.protrack.packaging.repository.PackageRepository;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectStageInfo;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.storage.StoragePort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Streams a production package as a {@code .zip} of its referenced document versions, and records the
 * download. The manifest is snapshotted inside a transaction (which increments the download count);
 * the bytes are streamed afterwards so large packages never buffer in memory.
 */
@Service
public class PackageDownloadService {

	private final PackageRepository packageRepository;
	private final FilesFacade filesFacade;
	private final ProjectFacade projectFacade;
	private final IdentityFacade identityFacade;
	private final StoragePort storagePort;

	public PackageDownloadService(PackageRepository packageRepository, FilesFacade filesFacade,
			ProjectFacade projectFacade, IdentityFacade identityFacade, StoragePort storagePort) {
		this.packageRepository = packageRepository;
		this.filesFacade = filesFacade;
		this.projectFacade = projectFacade;
		this.identityFacade = identityFacade;
		this.storagePort = storagePort;
	}

	/** The zip manifest: its download filename and the resolved entries. */
	public record ZipManifest(String zipFileName, List<Entry> entries) {

		public record Entry(String entryName, String storageKey) {
		}
	}

	/** Snapshot the package contents and record the download (transactional). */
	@Transactional
	public ZipManifest prepare(UUID actor, UUID projectId) {
		requireProjectInOrg(actor, projectId);
		ProductionPackage pkg = packageRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
				.orElseThrow(() -> new NotFoundException("No production package for this project yet."));

		List<ZipManifest.Entry> entries = new ArrayList<>();
		int index = 1;
		for (PackageItem item : pkg.getItems()) {
			if (item.getDocumentId() == null) {
				continue;
			}
			Optional<FileRef> ref = filesFacade.resolveCurrentVersion(item.getDocumentId());
			if (ref.isEmpty()) {
				continue;
			}
			String entryName = "%02d_%s".formatted(index++, ref.get().fileName());
			entries.add(new ZipManifest.Entry(entryName, ref.get().storageKey()));
		}

		pkg.incrementDownloadCount();
		packageRepository.save(pkg);
		return new ZipManifest("package-" + projectId + ".zip", entries);
	}

	/** Stream the manifest's entries into a zip on the given output stream. */
	public void streamTo(ZipManifest manifest, OutputStream out) {
		try (ZipOutputStream zip = new ZipOutputStream(out)) {
			for (ZipManifest.Entry entry : manifest.entries()) {
				Resource resource = storagePort.load(entry.storageKey());
				zip.putNextEntry(new ZipEntry(entry.entryName()));
				try (InputStream in = resource.getInputStream()) {
					in.transferTo(zip);
				}
				zip.closeEntry();
			}
		} catch (IOException ex) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "DOWNLOAD_ERROR",
					"Failed to stream the package.");
		}
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
}
