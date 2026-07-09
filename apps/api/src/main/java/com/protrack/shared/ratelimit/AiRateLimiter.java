package com.protrack.shared.ratelimit;

import com.protrack.shared.error.ApiException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Per-user rate limiter for the cost-bearing AI-trigger endpoints (manuscript analysis, PDF
 * preflight, assistant chat). Reuses Resilience4j (already on the classpath for the outbound AI
 * call) with one keyed {@link RateLimiter} per user, refreshed every minute. Non-blocking: an
 * over-budget request fails fast with HTTP 429 rather than queueing a request thread.
 *
 * <p>The limit is configured via {@code protrack.ai.rate-limit.requests-per-minute} (read with
 * {@code @Value} so the existing {@code ProtrackProperties} record is unchanged).
 */
@Component
public class AiRateLimiter {

	private final RateLimiterRegistry registry;

	public AiRateLimiter(
			@Value("${protrack.ai.rate-limit.requests-per-minute:30}") int requestsPerMinute) {
		RateLimiterConfig config = RateLimiterConfig.custom()
				.limitForPeriod(Math.max(1, requestsPerMinute))
				.limitRefreshPeriod(Duration.ofMinutes(1))
				.timeoutDuration(Duration.ZERO) // fail fast — never block waiting for a permit
				.build();
		this.registry = RateLimiterRegistry.of(config);
	}

	/** Consume one permit for the given user; throw 429 when their per-minute budget is exhausted. */
	public void check(String userKey) {
		RateLimiter limiter = registry.rateLimiter("ai-user:" + userKey);
		if (!limiter.acquirePermission()) {
			throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED",
					"Too many AI requests. Please wait a moment and try again.");
		}
	}
}
