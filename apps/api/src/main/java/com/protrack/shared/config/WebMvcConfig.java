package com.protrack.shared.config;

import com.protrack.shared.ratelimit.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the per-user AI-endpoint rate-limit interceptor on the cost-bearing LLM-trigger paths
 * (manuscript analysis, PDF preflight, assistant chat). Additive MVC configuration — it does not
 * replace Spring Boot's auto-configured MVC (pageable resolvers, message converters, etc.).
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private static final String[] AI_TRIGGER_PATHS = {
			"/api/v1/projects/*/analysis",
			"/api/v1/projects/*/preflight",
			"/api/v1/projects/*/assistant/messages",
	};

	private final RateLimitInterceptor rateLimitInterceptor;

	public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor) {
		this.rateLimitInterceptor = rateLimitInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(rateLimitInterceptor).addPathPatterns(AI_TRIGGER_PATHS);
	}
}
