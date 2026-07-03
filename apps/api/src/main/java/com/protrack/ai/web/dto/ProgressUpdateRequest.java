package com.protrack.ai.web.dto;

import java.util.Map;

/**
 * Progress callback posted by the FastAPI AI service during a job. Field names match what the AI
 * service sends ({@code progressPct}, {@code status}, {@code partial}); terminal states are owned by
 * the worker, so a callback never downgrades a finished job.
 */
public record ProgressUpdateRequest(
		Integer progressPct,
		String status,
		Map<String, Object> partial) {
}
