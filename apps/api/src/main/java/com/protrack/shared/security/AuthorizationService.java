package com.protrack.shared.security;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Permission-evaluation helper exposed to SpEL as {@code @authz}, for fine-grained checks in
 * {@code @PreAuthorize} — e.g. {@code @PreAuthorize("@authz.hasPermission('PROJECT_CREATE')")}.
 *
 * <p>Role authorities are stored as {@code ROLE_<code>}; permission authorities are the raw
 * permission code. Both come from the JWT (see {@link JwtAuthenticationFilter}).
 */
@Component("authz")
public class AuthorizationService {

	/** True if the current user holds the given permission code. */
	public boolean hasPermission(String permission) {
		return authorities().anyMatch(permission::equals);
	}

	/** True if the current user holds any of the given role codes (without the ROLE_ prefix). */
	public boolean hasAnyRole(String... roles) {
		Set<String> wanted = Set.of(roles);
		return authorities()
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> authority.substring("ROLE_".length()))
				.anyMatch(wanted::contains);
	}

	/** Role codes (without the ROLE_ prefix) held by the current user. */
	public Set<String> currentRoles() {
		return authorities()
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> authority.substring("ROLE_".length()))
				.collect(Collectors.toSet());
	}

	private Stream<String> authorities() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return Stream.empty();
		}
		return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
	}
}
