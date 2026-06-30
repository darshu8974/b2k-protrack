package com.protrack.identity.web;

import com.protrack.identity.service.UserService;
import com.protrack.identity.web.dto.AdminUserSummary;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrator-only user directory. Demonstrates method-level authorization: the URL is merely
 * "authenticated" in the security chain, while {@code @PreAuthorize} enforces the ADMIN role
 * (non-admins receive 403 via the access-denied handler).
 */
@RestController
@RequestMapping("/api/v1/admin")
public class UserAdminController {

	private final UserService userService;

	public UserAdminController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public List<AdminUserSummary> listUsers() {
		return userService.listUsers();
	}
}
