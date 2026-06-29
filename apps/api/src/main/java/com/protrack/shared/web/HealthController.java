package com.protrack.shared.web;

import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight public liveness endpoint for the SPA and uptime checks. Operational health/metrics
 * are served separately by Spring Boot Actuator ({@code /actuator/health}, {@code /actuator/prometheus}).
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

	@Value("${spring.application.name}")
	private String service;

	@Value("${protrack.version:0.1.0}")
	private String version;

	@GetMapping("/health")
	public HealthStatus health() {
		return new HealthStatus("UP", service, version, OffsetDateTime.now());
	}

	/** Minimal status payload. */
	public record HealthStatus(String status, String service, String version, OffsetDateTime timestamp) {
	}
}
