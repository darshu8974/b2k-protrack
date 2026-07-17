package com.protrack.preflight.web;

import com.protrack.ai.service.PreflightOrchestrator;
import com.protrack.ai.web.dto.AiJobResponse;
import com.protrack.preflight.service.PreflightResultService;
import com.protrack.preflight.service.ProductionPdfService;
import com.protrack.preflight.web.dto.PreflightDetailResponse;
import com.protrack.preflight.web.dto.ProductionPdfResponse;
import com.protrack.shared.error.NotFoundException;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Client-facing preflight endpoints: the paginator uploads the production PDF (auto-advancing to
 * PDF_REVIEW); QC/PM run preflight (async, 202); any member reads the latest result.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class PreflightController {

	private final ProductionPdfService productionPdfService;
	private final PreflightOrchestrator preflightOrchestrator;
	private final PreflightResultService preflightResultService;

	public PreflightController(ProductionPdfService productionPdfService,
			PreflightOrchestrator preflightOrchestrator,
			PreflightResultService preflightResultService) {
		this.productionPdfService = productionPdfService;
		this.preflightOrchestrator = preflightOrchestrator;
		this.preflightResultService = preflightResultService;
	}

	@PostMapping(path = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('PAGINATOR', 'ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public ProductionPdfResponse uploadPdf(@PathVariable UUID projectId,
			@RequestParam(name = "title", required = false) String title,
			@RequestParam("file") MultipartFile file, Principal principal) {
		return productionPdfService.submit(currentUserId(principal), projectId, file, title);
	}

	@PostMapping("/preflight")
	@PreAuthorize("hasAnyRole('QC', 'PROJECT_MANAGER', 'ADMIN')")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public AiJobResponse start(@PathVariable UUID projectId, Principal principal) {
		return preflightOrchestrator.startPreflight(currentUserId(principal), projectId);
	}

	@GetMapping("/preflight")
	public PreflightDetailResponse latest(@PathVariable UUID projectId) {
		return preflightResultService.getLatest(projectId)
				.orElseThrow(() -> new NotFoundException("No preflight for this project yet."));
	}

	private static UUID currentUserId(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
