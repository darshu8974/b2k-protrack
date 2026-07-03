package com.protrack.ai.web;

import com.protrack.ai.service.AiCallbackService;
import com.protrack.ai.web.dto.ProgressUpdateRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal endpoint the FastAPI AI service calls to report progress. Guarded by
 * {@link com.protrack.shared.security.InternalKeyFilter} (shared key), not JWT.
 */
@RestController
@RequestMapping("/internal/v1/ai-jobs")
public class InternalAiCallbackController {

	private final AiCallbackService callbackService;

	public InternalAiCallbackController(AiCallbackService callbackService) {
		this.callbackService = callbackService;
	}

	@PostMapping("/{jobId}/progress")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void progress(@PathVariable UUID jobId, @RequestBody ProgressUpdateRequest update) {
		callbackService.recordProgress(jobId, update);
	}
}
