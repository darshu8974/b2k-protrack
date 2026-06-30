package com.protrack.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads a Bearer token, validates it, and populates the SecurityContext with the user's
 * authorities (ROLE_* from roles, plus raw permission codes). Requests without a token, or with an
 * invalid token, simply proceed unauthenticated — the authorization rules + entry point handle the
 * 401. No login logic here (Task 1.3).
 *
 * <p>Not a Spring bean (constructed in SecurityConfig) so it runs only inside the security chain,
 * not the plain servlet filter chain.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String HEADER = "Authorization";
	private static final String PREFIX = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String token = resolveToken(request);
		if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				Claims claims = jwtService.parse(token);
				var authentication = new UsernamePasswordAuthenticationToken(
						claims.getSubject(), null, toAuthorities(claims));
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (JwtException | IllegalArgumentException ex) {
				// Invalid/expired token: leave the context empty; the entry point returns 401.
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String header = request.getHeader(HEADER);
		if (StringUtils.hasText(header) && header.startsWith(PREFIX)) {
			return header.substring(PREFIX.length());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Collection<SimpleGrantedAuthority> toAuthorities(Claims claims) {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		List<String> roles = claims.get("roles", List.class);
		if (roles != null) {
			roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
		}
		List<String> permissions = claims.get("permissions", List.class);
		if (permissions != null) {
			permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
		}
		return authorities;
	}
}
