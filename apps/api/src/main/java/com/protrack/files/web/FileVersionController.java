package com.protrack.files.web;

import com.protrack.files.service.FileVersionService;
import com.protrack.files.service.FileVersionService.Download;
import com.protrack.files.web.dto.DocumentResponse;
import com.protrack.files.web.dto.FileVersionResponse;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** File version history, new-version upload, rollback, and download. */
@RestController
@RequestMapping("/api/v1")
public class FileVersionController {

	private final FileVersionService fileVersionService;

	public FileVersionController(FileVersionService fileVersionService) {
		this.fileVersionService = fileVersionService;
	}

	@GetMapping("/documents/{documentId}/versions")
	public List<FileVersionResponse> versions(@PathVariable UUID documentId) {
		return fileVersionService.listVersions(documentId);
	}

	@PostMapping(path = "/documents/{documentId}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('PM', 'DESIGNER', 'ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public FileVersionResponse addVersion(@PathVariable UUID documentId,
			@RequestParam("file") MultipartFile file, Principal principal) {
		return fileVersionService.addVersion(currentUserId(principal), documentId, file);
	}

	@PostMapping("/documents/{documentId}/versions/{versionId}:setCurrent")
	@PreAuthorize("hasAnyRole('PM', 'ADMIN')")
	public DocumentResponse setCurrent(@PathVariable UUID documentId, @PathVariable UUID versionId,
			Principal principal) {
		return fileVersionService.setCurrent(currentUserId(principal), documentId, versionId);
	}

	@GetMapping("/file-versions/{versionId}/download")
	public ResponseEntity<Resource> download(@PathVariable UUID versionId) {
		Download download = fileVersionService.download(versionId);
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(download.fileName()).build();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.contentType(MediaType.parseMediaType(download.mimeType()))
				.contentLength(download.sizeBytes())
				.body(download.resource());
	}

	private static UUID currentUserId(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
