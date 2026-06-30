package com.protrack.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps exceptions to the standardized {@link Problem} body (REST API Specification §1.5).
 *
 * <p>Every response carries the request {@code traceId} (set by
 * {@link com.protrack.shared.web.CorrelationIdFilter}). Validation failures (422) include
 * field-level errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Problem> handleApiException(ApiException ex, HttpServletRequest request) {
		return build(ex.getStatus(), ex.getCode(), ex.getMessage(), request, null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Problem> handleValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		List<Problem.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(GlobalExceptionHandler::toFieldError)
				.toList();
		return build(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR",
				"One or more fields are invalid.", request, fieldErrors);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Problem> handleAccessDenied(AccessDeniedException ex,
			HttpServletRequest request) {
		// Method-level @PreAuthorize denials are dispatched here (URL-level denials are handled by
		// the security access-denied handler). Map both to a standard 403.
		return build(HttpStatus.FORBIDDEN, "FORBIDDEN",
				"You do not have permission to perform this action.", request, null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Problem> handleUnexpected(Exception ex, HttpServletRequest request) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
				"An unexpected error occurred.", request, null);
	}

	private static Problem.FieldError toFieldError(FieldError error) {
		return new Problem.FieldError(error.getField(), error.getCode(), error.getDefaultMessage());
	}

	private static ResponseEntity<Problem> build(HttpStatus status, String code, String detail,
			HttpServletRequest request, List<Problem.FieldError> fieldErrors) {
		Problem problem = new Problem(
				"https://protrack.app/errors/" + code.toLowerCase(),
				status.getReasonPhrase(),
				status.value(),
				code,
				detail,
				request.getRequestURI(),
				MDC.get("traceId"),
				OffsetDateTime.now(),
				fieldErrors);
		return ResponseEntity.status(status).body(problem);
	}
}
