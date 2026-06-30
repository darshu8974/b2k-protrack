package com.protrack.identity.service;

import com.protrack.identity.domain.Permission;
import com.protrack.identity.domain.Role;
import com.protrack.identity.domain.User;
import com.protrack.identity.repository.UserRepository;
import com.protrack.identity.web.dto.LoginRequest;
import com.protrack.identity.web.dto.TokenResponse;
import com.protrack.identity.web.dto.UserSummary;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.security.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Authenticates users against the seeded accounts and issues JWT access tokens. */
@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			JwtService jwtService, RefreshTokenService refreshTokenService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
	}

	/** Validate credentials, issue an access token, and return the authenticated user. */
	@Transactional
	public TokenResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		User user = userRepository.findByEmail(email)
				.orElseThrow(AuthService::invalidCredentials);

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw invalidCredentials();
		}
		if (!user.isActive()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE",
					"This account is not active.");
		}

		user.setLastLoginAt(Instant.now());
		String refreshToken = refreshTokenService.issue(user.getId());
		return issueTokens(user, refreshToken);
	}

	/**
	 * Rotate a refresh token: revoke the presented one and issue a fresh access + refresh pair.
	 *
	 * <p>Intentionally not {@code @Transactional} so the rotation (and any reuse-driven family
	 * revocation) commits inside {@link RefreshTokenService#rotate} rather than being rolled back
	 * by an outer transaction when a 401 is signalled. The user lookup eagerly fetches roles and
	 * permissions, so they remain readable after its transaction closes.
	 */
	public TokenResponse refresh(String refreshToken) {
		RefreshTokenService.Rotation rotation = refreshTokenService.rotate(refreshToken);
		User user = userRepository.findWithRolesById(rotation.userId())
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
						"Authenticated user no longer exists."));
		return issueTokens(user, rotation.newRawToken());
	}

	/** Revoke a refresh token (logout). Idempotent. */
	@Transactional
	public void logout(String refreshToken) {
		refreshTokenService.revoke(refreshToken);
	}

	private TokenResponse issueTokens(User user, String refreshToken) {
		List<String> roles = roleCodes(user);
		List<String> permissions = permissionCodes(user);
		String accessToken = jwtService.generateAccessToken(
				user.getId().toString(), user.getEmail(), roles, permissions);
		return new TokenResponse(accessToken, refreshToken, "Bearer",
				jwtService.getAccessTtlSeconds(), toSummary(user, roles, permissions));
	}

	/** Return details for the currently authenticated user (subject = user id). */
	@Transactional(readOnly = true)
	public UserSummary currentUser(String userId) {
		User user = userRepository.findWithRolesById(UUID.fromString(userId))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
						"Authenticated user no longer exists."));
		return toSummary(user, roleCodes(user), permissionCodes(user));
	}

	private static ApiException invalidCredentials() {
		return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
				"Invalid email or password.");
	}

	private static List<String> roleCodes(User user) {
		return user.getRoles().stream().map(Role::getCode).sorted().toList();
	}

	private static List<String> permissionCodes(User user) {
		return user.getRoles().stream()
				.flatMap(role -> role.getPermissions().stream())
				.map(Permission::getCode)
				.distinct()
				.sorted()
				.toList();
	}

	private static UserSummary toSummary(User user, List<String> roles, List<String> permissions) {
		return new UserSummary(
				user.getId().toString(),
				user.getEmail(),
				user.getFullName(),
				user.getAvatarInitials(),
				user.getAvatarColor(),
				roles,
				permissions);
	}
}
