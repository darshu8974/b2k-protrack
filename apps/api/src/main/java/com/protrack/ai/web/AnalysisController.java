package com.protrack.ai.web;

import com.protrack.ai.service.AnalysisOrchestrator;
import com.protrack.ai.web.dto.AiJobResponse;
import com.protrack.analysis.service.AnalysisResultService;
import com.protrack.analysis.web.dto.AnalysisDetailResponse;
import com.protrack.shared.error.NotFoundException;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Client-facing manuscript-analysis endpoints (start + latest result). */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/analysis")
public class AnalysisController {

	private final AnalysisOrchestrator orchestrator;
	private final AnalysisResultService analysisResultService;

	public AnalysisController(AnalysisOrchestrator orchestrator,
			AnalysisResultService analysisResultService) {
		this.orchestrator = orchestrator;
		this.analysisResultService = analysisResultService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public AiJobResponse start(@PathVariable UUID projectId, Principal principal) {
		return orchestrator.startManuscriptAnalysis(UUID.fromString(principal.getName()), projectId);
	}

	@GetMapping
	public AnalysisDetailResponse latest(@PathVariable UUID projectId) {
		return analysisResultService.getLatest(projectId)
				.orElseThrow(() -> new NotFoundException("No analysis for this project yet."));
	}
}
