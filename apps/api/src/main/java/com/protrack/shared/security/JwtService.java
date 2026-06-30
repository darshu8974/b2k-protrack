package com.protrack.shared.security;

import com.protrack.shared.properties.ProtrackProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Issues and validates HS256 access tokens. This is the JWT foundation only — login (which calls
 * {@link #generateAccessToken}) lands in Task 1.3, and refresh tokens are a later task.
 */
@Service
public class JwtService {

	private final SecretKey key;
	private final long accessTtlSeconds;

	public JwtService(ProtrackProperties properties) {
		ProtrackProperties.Jwt jwt = properties.jwt();
		this.key = Keys.hmacShaKeyFor(jwt.secret().getBytes(StandardCharsets.UTF_8));
		this.accessTtlSeconds = jwt.accessTtl().toSeconds();
	}

	/** Build a signed access token carrying the user's id (subject), email, roles and permissions. */
	public String generateAccessToken(String userId, String email, Collection<String> roles,
			Collection<String> permissions) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(userId)
				.claim("email", email)
				.claim("roles", List.copyOf(roles))
				.claim("permissions", List.copyOf(permissions))
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
				.signWith(key)
				.compact();
	}

	/** Parse and verify a token, returning its claims. Throws JwtException if invalid/expired. */
	public Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
