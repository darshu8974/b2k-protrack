package com.protrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Protrack API — Spring Boot modular monolith entry point.
 *
 * <p>Business modules (identity, project, workflow, files, ai, analysis, preflight, qa, packaging,
 * notification, audit, reporting, assistant) are added in later sprints under {@code com.protrack}.
 * This Sprint-0 scaffold contains only cross-cutting infrastructure in {@code com.protrack.shared}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ProtrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProtrackApplication.class, args);
	}
}
