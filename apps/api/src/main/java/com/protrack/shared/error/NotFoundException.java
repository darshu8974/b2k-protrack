package com.protrack.shared.error;

import org.springframework.http.HttpStatus;

/** Thrown when a requested resource does not exist or is not visible to the caller (404). */
public class NotFoundException extends ApiException {

	public NotFoundException(String message) {
		super(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
	}
}
