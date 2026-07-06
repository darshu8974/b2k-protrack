plugins {
	java
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.protrack"
version = "0.1.0-SNAPSHOT"
description = "Protrack API — Spring Boot modular monolith"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["springdocVersion"] = "2.7.0"
extra["jjwtVersion"] = "0.12.6"
extra["resilience4jVersion"] = "2.2.0"

dependencies {
	// Web + API
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdocVersion")}")

	// Security + JWT
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("io.jsonwebtoken:jjwt-api:${property("jjwtVersion")}")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwtVersion")}")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwtVersion")}")

	// Outbound email (in-app + email notifications, Sprint 6)
	implementation("org.springframework.boot:spring-boot-starter-mail")

	// Persistence + migrations
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	// Resilience for the outbound AI service call (retry + circuit breaker over RestClient)
	implementation("io.github.resilience4j:resilience4j-spring-boot3:${property("resilience4jVersion")}")
	implementation("org.springframework.boot:spring-boot-starter-aop")

	// Observability
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	// Typed configuration metadata
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
