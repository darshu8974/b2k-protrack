package com.protrack.ai.service;

import com.protrack.ai.domain.JobType;
import java.util.UUID;

/**
 * Per-job-type strategy for the {@link AiJobWorker}. The worker owns the generic scaffolding
 * (mark RUNNING, the transaction boundaries, SSE, failure handling); a handler owns only the two
 * type-specific steps: the external FastAPI call (outside any transaction) and persisting the
 * normalized result while marking the job SUCCEEDED (inside the worker's second transaction).
 */
public interface AiJobHandler {

	JobType jobType();

	/** Call the FastAPI AI service. Runs outside any transaction; the returned holder is opaque. */
	Object callAiService(UUID jobId, AiJobContext context);

	/**
	 * Persist the normalized result, mark the job SUCCEEDED, and publish the completion event.
	 * Invoked inside the worker's second transaction, so the writes are atomic.
	 */
	void persistResult(UUID jobId, AiJobContext context, Object result);
}
