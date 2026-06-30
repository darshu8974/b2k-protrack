package com.protrack.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.protrack.shared.error.Problem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Returns a standardized {@link Problem} 403 when an authenticated user lacks the required role. */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public RestAccessDeniedHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		Problem problem = new Problem(
				"https://protrack.app/errors/forbidden",
				HttpStatus.FORBIDDEN.getReasonPhrase(),
				HttpStatus.FORBIDDEN.value(),
				"FORBIDDEN",
				"You do not have permission to perform this action.",
				request.getRequestURI(),
				MDC.get("traceId"),
				OffsetDateTime.now(),
				null);
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), problem);
	}
}
