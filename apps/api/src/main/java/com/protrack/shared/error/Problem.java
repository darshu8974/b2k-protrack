package com.protrack.shared.error;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Standardized error response body (RFC 9457-style Problem Details) as defined in the approved
 * REST API Specification (§1.5). Returned by {@link GlobalExceptionHandler} for every error.
 *
 * @param fieldErrors populated for validation (422) failures; otherwise null/empty.
 */
public record Problem(
		String type,
		String title,
		int status,
		String code,
		String detail,
		String instance,
		String traceId,
		OffsetDateTime timestamp,
		List<FieldError> fieldErrors) {

	/** A single field-level validation error. */
	public record FieldError(String field, String code, String message) {
	}
}
