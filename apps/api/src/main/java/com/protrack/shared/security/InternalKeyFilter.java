package com.protrack.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Guards the internal service-to-service endpoints ({@code /internal/v1/**}, e.g. the AI progress
 * callback) with the shared {@code X-Internal-Key}. Only the FastAPI AI service calls these; browser
 * clients never do. Non-internal paths pass straight through (JWT security handles those).
 *
 * <p>Constructed in {@link com.protrack.shared.config.SecurityConfig} so it runs inside the security
 * chain, not the plain servlet chain.
 */
public class InternalKeyFilter extends OncePerRequestFilter {

	private static final String INTERNAL_PREFIX = "/internal/v1/";
	private static final String HEADER = "X-Internal-Key";

	private final byte[] expectedKey;

	public InternalKeyFilter(String internalKey) {
		this.expectedKey = internalKey.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (!request.getRequestURI().startsWith(INTERNAL_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}
		String provided = request.getHeader(HEADER);
		if (!StringUtils.hasText(provided) || !MessageDigest.isEqual(
				provided.getBytes(StandardCharsets.UTF_8), expectedKey)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write(
					"{\"code\":\"UNAUTHORIZED\",\"detail\":\"Invalid or missing internal key\"}");
			return;
		}
		filterChain.doFilter(request, response);
	}
}
