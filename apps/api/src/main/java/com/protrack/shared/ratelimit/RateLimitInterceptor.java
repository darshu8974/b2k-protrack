package com.protrack.shared.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Applies {@link AiRateLimiter} per authenticated user to the AI-trigger endpoints (path patterns
 * registered in {@code WebMvcConfig}). Runs after the security filter chain, so the JWT subject
 * (the user id, as used by {@code Principal.getName()}) is already in the security context. Only
 * mutating POSTs are limited — GET reads that share the same path are left untouched.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

	private final AiRateLimiter rateLimiter;

	public RateLimitInterceptor(AiRateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) {
		if (!HttpMethod.POST.matches(request.getMethod())) {
			return true;
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
			rateLimiter.check(auth.getName()); // throws 429 when the per-user budget is exhausted
		}
		return true;
	}
}
