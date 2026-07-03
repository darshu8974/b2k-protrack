package com.protrack.ai.service;

import java.util.UUID;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * In-process {@link AiJobDispatcher} that runs the job on the bounded {@code aiExecutor} pool, so the
 * request thread returns 202 immediately while the FastAPI call happens off the request path.
 */
@Component
public class AsyncAiJobDispatcher implements AiJobDispatcher {

	private final AiJobWorker worker;

	public AsyncAiJobDispatcher(AiJobWorker worker) {
		this.worker = worker;
	}

	@Override
	@Async("aiExecutor")
	public void dispatch(UUID jobId) {
		worker.process(jobId);
	}
}
