package com.protrack.shared.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration for Protrack, bound from the {@code protrack.*} namespace.
 *
 * <p>This is the structural skeleton only — values are supplied per profile/environment. The
 * individual sections are consumed by their owning modules in later sprints (JWT by identity,
 * storage by files, ai by the ai module, mail by notification).
 */
@ConfigurationProperties(prefix = "protrack")
public record ProtrackProperties(
		Jwt jwt,
		Storage storage,
		Ai ai,
		Mail mail) {

	/** JWT settings (consumed by the identity module, Sprint 1). */
	public record Jwt(String secret, Duration accessTtl, Duration refreshTtl) {
	}

	/** File storage settings; driver = {@code local} (Phase 1) or {@code s3} (future). */
	public record Storage(String driver, String localPath) {
	}

	/** FastAPI AI service connection settings (consumed by the ai module, Sprint 4). */
	public record Ai(String baseUrl, String internalKey, long timeoutMs) {
	}

	/**
	 * Outbound mail settings (consumed by the notification module, Sprint 6).
	 *
	 * <p>{@code enabled} selects the delivery adapter: {@code true} → {@code SmtpMailAdapter}
	 * (requires {@code spring.mail.*}); {@code false} (default) → {@code LogMailAdapter}, which
	 * logs the message instead of sending it (dev/demo/test). {@code from} is the sender address.
	 */
	public record Mail(boolean enabled, String from) {
	}
}
