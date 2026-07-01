package com.protrack.packaging.web;

import com.protrack.packaging.service.PackageAssemblyService;
import com.protrack.packaging.service.PackageDownloadService;
import com.protrack.packaging.service.PackageDownloadService.ZipManifest;
import com.protrack.packaging.web.dto.AddPackageItemRequest;
import com.protrack.packaging.web.dto.PackageResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/** Production package: view, assemble, curate items, and download the hand-off zip. */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/package")
public class PackageController {

	private final PackageAssemblyService assemblyService;
	private final PackageDownloadService downloadService;

	public PackageController(PackageAssemblyService assemblyService,
			PackageDownloadService downloadService) {
		this.assemblyService = assemblyService;
		this.downloadService = downloadService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('DESIGNER', 'PM', 'ADMIN')")
	public PackageResponse get(@PathVariable UUID projectId, Principal principal) {
		return assemblyService.get(currentUserId(principal), projectId);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('PM', 'ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public PackageResponse assemble(@PathVariable UUID projectId, Principal principal) {
		return assemblyService.assemble(currentUserId(principal), projectId);
	}

	@PostMapping("/items")
	@PreAuthorize("hasAnyRole('PM', 'ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public PackageResponse addItem(@PathVariable UUID projectId,
			@Valid @RequestBody AddPackageItemRequest request, Principal principal) {
		return assemblyService.addItem(currentUserId(principal), projectId, request);
	}

	@DeleteMapping("/items/{itemId}")
	@PreAuthorize("hasAnyRole('PM', 'ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeItem(@PathVariable UUID projectId, @PathVariable UUID itemId,
			Principal principal) {
		assemblyService.removeItem(currentUserId(principal), projectId, itemId);
	}

	@GetMapping("/download")
	@PreAuthorize("hasAnyRole('DESIGNER', 'PM', 'ADMIN')")
	public ResponseEntity<StreamingResponseBody> download(@PathVariable UUID projectId,
			Principal principal) {
		ZipManifest manifest = downloadService.prepare(currentUserId(principal), projectId);
		StreamingResponseBody body = out -> downloadService.streamTo(manifest, out);
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(manifest.zipFileName()).build();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.contentType(MediaType.parseMediaType("application/zip"))
				.body(body);
	}

	private static UUID currentUserId(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
