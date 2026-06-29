package com.protrack.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc / OpenAPI document metadata for the Protrack API.
 *
 * <p>Security schemes and per-endpoint documentation are added alongside Spring Security in Sprint 1.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI protrackOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Protrack API")
						.version("v1")
						.description("AI Publishing Operating System — Phase 1 MVP REST API.")
						.license(new License().name("Proprietary")))
				.servers(List.of(new Server().url("/").description("Default server")));
	}
}
