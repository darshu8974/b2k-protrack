package com.protrack.ai.health;

import com.protrack.shared.properties.ProtrackProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Actuator health component ("aiService") reporting reachability of the FastAPI AI service. Uses a
 * dedicated short-timeout client against the service's public liveness endpoint, so a slow or
 * unreachable AI service is surfaced quickly without going through the business client's long
 * timeout + retries.
 *
 * <p>The AI service is a <em>soft</em> dependency — it is excluded from the readiness probe group
 * (see {@code application.yml}), so a transient AI outage does not fail the app's readiness (the
 * non-AI features stay available).
 */
@Component
public class AiServiceHealthIndicator implements HealthIndicator {

	private static final int CONNECT_TIMEOUT_MS = 1_000;
	private static final int READ_TIMEOUT_MS = 2_000;

	private final RestClient client;
	private final String baseUrl;

	public AiServiceHealthIndicator(ProtrackProperties properties) {
		this.baseUrl = properties.ai().baseUrl();
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
		factory.setReadTimeout(READ_TIMEOUT_MS);
		this.client = RestClient.builder().requestFactory(factory).baseUrl(baseUrl).build();
	}

	@Override
	public Health health() {
		try {
			client.get().uri("/internal/v1/health").retrieve().toBodilessEntity();
			return Health.up().withDetail("baseUrl", baseUrl).build();
		} catch (Exception ex) {
			return Health.down().withDetail("baseUrl", baseUrl)
					.withDetail("error", ex.getClass().getSimpleName()).build();
		}
	}
}
