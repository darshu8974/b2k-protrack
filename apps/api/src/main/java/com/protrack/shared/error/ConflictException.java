package com.protrack.shared.error;

import org.springframework.http.HttpStatus;

/** Thrown on a state conflict — duplicate resource or an illegal workflow transition (409). */
public class ConflictException extends ApiException {

	public ConflictException(String code, String message) {
		super(HttpStatus.CONFLICT, code, message);
	}
}
