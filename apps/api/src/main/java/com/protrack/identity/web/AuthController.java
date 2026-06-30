package com.protrack.identity.web;

import com.protrack.identity.service.AuthService;
import com.protrack.identity.web.dto.LoginRequest;
import com.protrack.identity.web.dto.RefreshTokenRequest;
import com.protrack.identity.web.dto.TokenResponse;
import com.protrack.identity.web.dto.UserSummary;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Authentication endpoints. Login is public; /me requires a valid access token. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/refresh")
	public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authService.refresh(request.refreshToken());
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@Valid @RequestBody RefreshTokenRequest request) {
		authService.logout(request.refreshToken());
	}

	@GetMapping("/me")
	public UserSummary me(Principal principal) {
		// principal.getName() is the JWT subject = the user id.
		return authService.currentUser(principal.getName());
	}
}
