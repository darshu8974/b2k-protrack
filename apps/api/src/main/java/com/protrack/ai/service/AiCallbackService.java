package com.protrack.ai.service;

import com.protrack.ai.domain.AiJob;
import com.protrack.ai.domain.JobStatus;
import com.protrack.ai.repository.AiJobRepository;
import com.protrack.ai.web.dto.ProgressUpdateRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies AI progress callbacks to the job and relays them to SSE subscribers. Terminal states
 * (SUCCEEDED/FAILED) are owned by the worker, so a late callback never downgrades a finished job.
 */
@Service
public class AiCallbackService {

	private final AiJobRepository aiJobRepository;
	private final SseService sseService;

	public AiCallbackService(AiJobRepository aiJobRepository, SseService sseService) {
		this.aiJobRepository = aiJobRepository;
		this.sseService = sseService;
	}

	@Transactional
	public void recordProgress(UUID jobId, ProgressUpdateRequest update) {
		AiJob job = aiJobRepository.findById(jobId).orElse(null);
		if (job == null) {
			return;
		}
		boolean terminal = JobStatus.SUCCEEDED.name().equals(job.getStatus())
				|| JobStatus.FAILED.name().equals(job.getStatus());
		if (terminal) {
			return;
		}
		int pct = update.progressPct() == null ? job.getProgressPct() : update.progressPct();
		JobStatus status = parseNonTerminal(update.status());
		job.updateProgress(pct, status);
		aiJobRepository.save(job);

		Map<String, Object> payload = new HashMap<>();
		payload.put("jobId", jobId.toString());
		payload.put("progressPct", pct);
		payload.put("status", status == null ? job.getStatus() : status.name());
		sseService.publish(job.getProjectId(), "progress", payload);
	}

	/** Callbacks may only move a job to RUNNING; terminal transitions are the worker's job. */
	private static JobStatus parseNonTerminal(String status) {
		if (status == null) {
			return null;
		}
		try {
			JobStatus parsed = JobStatus.valueOf(status);
			return parsed == JobStatus.RUNNING ? JobStatus.RUNNING : null;
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}
}
