package com.protrack.ai.web;

import com.protrack.ai.repository.AiJobRepository;
import com.protrack.ai.web.dto.AiJobResponse;
import com.protrack.shared.error.NotFoundException;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI job status/progress (SSE fallback polling target). */
@RestController
@RequestMapping("/api/v1/ai-jobs")
public class AiJobController {

	private final AiJobRepository aiJobRepository;

	public AiJobController(AiJobRepository aiJobRepository) {
		this.aiJobRepository = aiJobRepository;
	}

	@GetMapping("/{jobId}")
	public AiJobResponse get(@PathVariable UUID jobId) {
		return aiJobRepository.findById(jobId)
				.map(AiJobResponse::from)
				.orElseThrow(() -> new NotFoundException("AI job not found."));
	}
}
