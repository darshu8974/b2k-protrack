package com.protrack.ai.client;

/**
 * A transient failure calling the AI service (5xx, connection error). Retryable — the Resilience4j
 * retry/circuit-breaker instances are configured to act on this type.
 */
public class AiServiceUnavailableException extends RuntimeException {

	public AiServiceUnavailableException(String message) {
		super(message);
	}

	public AiServiceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
