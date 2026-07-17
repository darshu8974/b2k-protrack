package com.protrack.files.web;

import com.protrack.files.domain.DocType;
import com.protrack.files.service.DocumentService;
import com.protrack.files.web.dto.DocumentResponse;
import com.protrack.files.web.dto.DocumentSummaryResponse;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Document + manuscript upload endpoints (versioning lives in {@link FileVersionController}). */
@RestController
@RequestMapping("/api/v1")
public class DocumentController {

	private final DocumentService documentService;

	public DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@GetMapping("/projects/{projectId}/documents")
	public List<DocumentSummaryResponse> list(@PathVariable UUID projectId,
			@RequestParam(name = "docType", required = false) String docType, Principal principal) {
		return documentService.list(currentUserId(principal), projectId, docType);
	}

	@PostMapping(path = "/projects/{projectId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'PAGINATOR', 'ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public DocumentResponse create(@PathVariable UUID projectId,
			@RequestParam("docType") DocType docType,
			@RequestParam(name = "title", required = false) String title,
			@RequestParam("file") MultipartFile file, Principal principal) {
		return documentService.create(currentUserId(principal), projectId, docType, title, file);
	}

	@PostMapping(path = "/projects/{projectId}/manuscript", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public DocumentResponse uploadManuscript(@PathVariable UUID projectId,
			@RequestParam(name = "title", required = false) String title,
			@RequestParam("file") MultipartFile file, Principal principal) {
		return documentService.uploadManuscript(currentUserId(principal), projectId, title, file);
	}

	@GetMapping("/documents/{documentId}")
	public DocumentResponse get(@PathVariable UUID documentId) {
		return documentService.get(documentId);
	}

	private static UUID currentUserId(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
