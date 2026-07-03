package com.protrack.ai.client;

/** A non-retryable failure calling the AI service (bad request, unparseable response). */
public class AiServiceException extends RuntimeException {

	public AiServiceException(String message) {
		super(message);
	}

	public AiServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
