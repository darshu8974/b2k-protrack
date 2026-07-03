package com.protrack.ai.service;

import java.util.UUID;

/**
 * Port that dispatches a queued AI job for processing. Today an {@code @Async} in-process worker;
 * moving to a queue (RabbitMQ/SQS) later is an adapter swap, not an orchestration change.
 */
public interface AiJobDispatcher {

	void dispatch(UUID jobId);
}
