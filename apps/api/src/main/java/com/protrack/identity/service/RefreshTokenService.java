package com.protrack.identity.service;

import com.protrack.identity.domain.RefreshToken;
import com.protrack.identity.repository.RefreshTokenRepository;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.properties.ProtrackProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages opaque refresh tokens. The raw token is a 256-bit random value returned to the client
 * once; only its SHA-256 hash is persisted. Refresh rotates (revokes the old, issues a new) and
 * detects reuse of an already-revoked token by revoking the whole family.
 */
@Service
public class RefreshTokenService {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

	private final RefreshTokenRepository repository;
	private final Duration refreshTtl;

	public RefreshTokenService(RefreshTokenRepository repository, ProtrackProperties properties) {
		this.repository = repository;
		this.refreshTtl = properties.jwt().refreshTtl();
	}

	/** Issue a new refresh token for a user; returns the raw token (shown to the client once). */
	@Transactional
	public String issue(UUID userId) {
		String rawToken = randomToken();
		Instant now = Instant.now();
		RefreshToken token = new RefreshToken(
				UUID.randomUUID(), userId, sha256(rawToken), now, now.plus(refreshTtl));
		repository.save(token);
		return rawToken;
	}

	/** Result of a rotation: the owning user and the new raw refresh token. */
	public record Rotation(UUID userId, String newRawToken) {
	}

	/**
	 * Validate and rotate a refresh token. Revokes the presented token and issues a fresh one.
	 *
	 * <p>{@code noRollbackFor = ApiException} so that, when token reuse is detected, the family
	 * revocation is committed even though a 401 is then signalled to the caller.
	 */
	@Transactional(noRollbackFor = ApiException.class)
	public Rotation rotate(String rawToken) {
		Instant now = Instant.now();
		RefreshToken current = repository.findByTokenHash(sha256(rawToken))
				.orElseThrow(RefreshTokenService::invalidToken);

		if (current.isRevoked()) {
			// Reuse of an already-rotated token: revoke the whole family as a precaution.
			revokeAllForUser(current.getUserId(), now);
			throw invalidToken();
		}
		if (current.isExpired(now)) {
			throw invalidToken();
		}

		String newRaw = randomToken();
		RefreshToken replacement = new RefreshToken(
				UUID.randomUUID(), current.getUserId(), sha256(newRaw), now, now.plus(refreshTtl));
		repository.save(replacement);

		current.setRevokedAt(now);
		current.setReplacedBy(replacement.getId());

		return new Rotation(current.getUserId(), newRaw);
	}

	/** Revoke a refresh token (logout). Idempotent and silent if the token is unknown. */
	@Transactional
	public void revoke(String rawToken) {
		repository.findByTokenHash(sha256(rawToken))
				.filter(token -> !token.isRevoked())
				.ifPresent(token -> token.setRevokedAt(Instant.now()));
	}

	private void revokeAllForUser(UUID userId, Instant now) {
		repository.findByUserIdAndRevokedAtIsNull(userId)
				.forEach(token -> token.setRevokedAt(now));
	}

	private static String randomToken() {
		byte[] bytes = new byte[32];
		SECURE_RANDOM.nextBytes(bytes);
		return URL_ENCODER.encodeToString(bytes);
	}

	private static String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception ex) {
			throw new IllegalStateException("SHA-256 not available", ex);
		}
	}

	private static ApiException invalidToken() {
		return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN",
				"Refresh token is invalid, expired, or revoked.");
	}
}
