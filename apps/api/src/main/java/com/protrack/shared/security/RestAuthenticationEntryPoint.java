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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** Returns a standardized {@link Problem} 401 when an unauthenticated request hits a secured route. */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		Problem problem = new Problem(
				"https://protrack.app/errors/unauthorized",
				HttpStatus.UNAUTHORIZED.getReasonPhrase(),
				HttpStatus.UNAUTHORIZED.value(),
				"UNAUTHORIZED",
				"Authentication is required to access this resource.",
				request.getRequestURI(),
				MDC.get("traceId"),
				OffsetDateTime.now(),
				null);
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), problem);
	}
}
