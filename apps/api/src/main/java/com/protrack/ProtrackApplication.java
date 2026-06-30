package com.protrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Protrack API — Spring Boot modular monolith entry point.
 *
 * <p>Cross-cutting infrastructure lives in {@code com.protrack.shared}; the {@code identity} module
 * (auth, JWT, RBAC) is implemented as of Sprint 1. Remaining business modules (project, workflow,
 * files, ai, analysis, preflight, qa, packaging, notification, audit, reporting, assistant) are
 * added in later sprints under {@code com.protrack}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ProtrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProtrackApplication.class, args);
	}
}
