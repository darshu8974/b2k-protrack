package com.protrack.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Base application exception carrying an HTTP status and a stable machine-readable error code.
 * Module-specific exceptions extend this; {@link GlobalExceptionHandler} maps them to {@link Problem}.
 */
public class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final String code;

	public ApiException(HttpStatus status, String code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}
}
