package com.protrack.identity.web;

import com.protrack.identity.service.UserService;
import com.protrack.identity.web.dto.AdminUserResponse;
import com.protrack.identity.web.dto.AssignRoleRequest;
import com.protrack.identity.web.dto.BulkUserRequest;
import com.protrack.identity.web.dto.BulkUserResult;
import com.protrack.identity.web.dto.CreateUserRequest;
import com.protrack.identity.web.dto.PermissionResponse;
import com.protrack.identity.web.dto.RoleResponse;
import com.protrack.identity.web.dto.UpdateUserRequest;
import com.protrack.shared.web.PageResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrator identity management (API Specification §3.2). The URL prefix ({@code /admin/*})
 * continues the Sprint-1 admin surface; RBAC is enforced per-method with {@code @PreAuthorize}
 * (users = ADMIN; roles reference = ADMIN/PM; permissions = ADMIN). Non-admins receive 403 via the
 * access-denied handler. Self-lockout guards live in {@link UserService}.
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
	public PageResponse<AdminUserResponse> listUsers(
			@RequestParam(required = false) String role,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String q,
			@PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable,
			Principal principal) {
		return userService.listUsers(currentUser(principal), role, status, q, pageable);
	}

	@PostMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public AdminUserResponse create(@Valid @RequestBody CreateUserRequest request, Principal principal) {
		return userService.createUser(currentUser(principal), request);
	}

	@GetMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminUserResponse get(@PathVariable UUID id, Principal principal) {
		return userService.getUser(currentUser(principal), id);
	}

	@PatchMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminUserResponse update(@PathVariable UUID id,
			@Valid @RequestBody UpdateUserRequest request, Principal principal) {
		return userService.updateUser(currentUser(principal), id, request);
	}

	@DeleteMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deactivate(@PathVariable UUID id, Principal principal) {
		userService.deactivateUser(currentUser(principal), id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/users/{id}/roles")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminUserResponse assignRole(@PathVariable UUID id,
			@Valid @RequestBody AssignRoleRequest request, Principal principal) {
		return userService.assignRole(currentUser(principal), id, request.roleId());
	}

	@DeleteMapping("/users/{id}/roles/{roleId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> revokeRole(@PathVariable UUID id, @PathVariable Integer roleId,
			Principal principal) {
		userService.revokeRole(currentUser(principal), id, roleId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/users:bulk")
	@PreAuthorize("hasRole('ADMIN')")
	public BulkUserResult bulk(@Valid @RequestBody BulkUserRequest request, Principal principal) {
		return userService.bulkUpdateStatus(currentUser(principal), request);
	}

	@GetMapping("/roles")
	@PreAuthorize("hasAnyRole('ADMIN', 'PM')")
	public List<RoleResponse> roles() {
		return userService.listRoles();
	}

	@GetMapping("/permissions")
	@PreAuthorize("hasRole('ADMIN')")
	public List<PermissionResponse> permissions() {
		return userService.listPermissions();
	}

	private static UUID currentUser(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
