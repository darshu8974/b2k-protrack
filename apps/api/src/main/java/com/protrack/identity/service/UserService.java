package com.protrack.identity.service;

import com.protrack.identity.domain.Role;
import com.protrack.identity.domain.User;
import com.protrack.identity.repository.PermissionRepository;
import com.protrack.identity.repository.RoleRepository;
import com.protrack.identity.repository.UserRepository;
import com.protrack.identity.repository.UserSpecifications;
import com.protrack.identity.web.dto.AdminUserResponse;
import com.protrack.identity.web.dto.BulkUserRequest;
import com.protrack.identity.web.dto.BulkUserResult;
import com.protrack.identity.web.dto.CreateUserRequest;
import com.protrack.identity.web.dto.PermissionResponse;
import com.protrack.identity.web.dto.RoleResponse;
import com.protrack.identity.web.dto.UpdateUserRequest;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.web.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Administrator user management (API Specification §3.2): list/create/update/deactivate users,
 * assign/revoke roles, and bulk status changes, plus role/permission reference reads. All operations
 * are scoped to the administrator's own organization. Self-protection guardrails prevent an admin
 * from locking themselves out (no self-deactivation; a user must retain at least one role).
 */
@Service
public class UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, RoleRepository roleRepository,
			PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.permissionRepository = permissionRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// ── reads ────────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public PageResponse<AdminUserResponse> listUsers(UUID adminId, String role, String status,
			String q, Pageable pageable) {
		UUID organizationId = organizationOf(adminId);
		Specification<User> spec = UserSpecifications.inOrganization(organizationId);
		if (StringUtils.hasText(status)) {
			spec = spec.and(UserSpecifications.hasStatus(status));
		}
		if (StringUtils.hasText(role)) {
			spec = spec.and(UserSpecifications.hasRole(role));
		}
		if (StringUtils.hasText(q)) {
			spec = spec.and(UserSpecifications.search(q));
		}
		Page<User> page = userRepository.findAll(spec, pageable);
		return PageResponse.of(page.map(AdminUserResponse::from));
	}

	@Transactional(readOnly = true)
	public AdminUserResponse getUser(UUID adminId, UUID userId) {
		return AdminUserResponse.from(loadInOrg(adminId, userId));
	}

	@Transactional(readOnly = true)
	public List<RoleResponse> listRoles() {
		return roleRepository.findAllByOrderByCodeAsc().stream().map(RoleResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public List<PermissionResponse> listPermissions() {
		return permissionRepository.findAllByOrderByCodeAsc().stream()
				.map(PermissionResponse::from).toList();
	}

	// ── writes ───────────────────────────────────────────────────────────────

	@Transactional
	public AdminUserResponse createUser(UUID adminId, CreateUserRequest request) {
		UUID organizationId = organizationOf(adminId);
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (userRepository.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_EMAIL",
					"A user with this email already exists.");
		}
		Role role = roleRepository.findById(request.roleId())
				.orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ROLE",
						"Unknown role."));

		User user = new User(UUID.randomUUID(), organizationId, email,
				passwordEncoder.encode(request.password()), request.fullName().trim(),
				initialsOf(request.fullName()), request.avatarColor());
		user.addRole(role);
		userRepository.save(user);
		return AdminUserResponse.from(user);
	}

	@Transactional
	public AdminUserResponse updateUser(UUID adminId, UUID userId, UpdateUserRequest request) {
		User user = loadInOrg(adminId, userId);
		if (request.status() != null && !"ACTIVE".equals(request.status())
				&& user.getId().equals(adminId)) {
			throw selfDeactivation();
		}
		user.updateProfile(
				StringUtils.hasText(request.fullName()) ? request.fullName().trim() : null,
				StringUtils.hasText(request.fullName()) ? initialsOf(request.fullName()) : null,
				request.avatarColor());
		if (request.status() != null) {
			user.changeStatus(request.status());
		}
		return AdminUserResponse.from(user);
	}

	@Transactional
	public void deactivateUser(UUID adminId, UUID userId) {
		User user = loadInOrg(adminId, userId);
		if (user.getId().equals(adminId)) {
			throw selfDeactivation();
		}
		user.changeStatus("INACTIVE");
	}

	@Transactional
	public AdminUserResponse assignRole(UUID adminId, UUID userId, Integer roleId) {
		User user = loadInOrg(adminId, userId);
		Role role = roleRepository.findById(roleId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ROLE",
						"Unknown role."));
		user.addRole(role);
		return AdminUserResponse.from(user);
	}

	@Transactional
	public AdminUserResponse revokeRole(UUID adminId, UUID userId, Integer roleId) {
		User user = loadInOrg(adminId, userId);
		Role role = roleRepository.findById(roleId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ROLE",
						"Unknown role."));
		if (user.getRoles().contains(role) && user.getRoles().size() == 1) {
			throw new ApiException(HttpStatus.CONFLICT, "LAST_ROLE",
					"A user must keep at least one role.");
		}
		user.removeRole(role);
		return AdminUserResponse.from(user);
	}

	@Transactional
	public BulkUserResult bulkUpdateStatus(UUID adminId, BulkUserRequest request) {
		UUID organizationId = organizationOf(adminId);
		String targetStatus = "DEACTIVATE".equals(request.action()) ? "INACTIVE" : "ACTIVE";
		boolean deactivating = "INACTIVE".equals(targetStatus);
		int updated = 0;
		int skipped = 0;
		for (UUID userId : request.userIds().stream().distinct().toList()) {
			User user = userRepository.findById(userId).orElse(null);
			// Skip unknown/cross-org users, and never let an admin deactivate themselves in bulk.
			if (user == null || !organizationId.equals(user.getOrganizationId())
					|| (deactivating && user.getId().equals(adminId))) {
				skipped++;
				continue;
			}
			user.changeStatus(targetStatus);
			updated++;
		}
		return new BulkUserResult(updated, skipped);
	}

	// ── helpers ────────────────────────────────────────────────────────────────

	private UUID organizationOf(UUID adminId) {
		return userRepository.findById(adminId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
						"Authenticated user no longer exists."))
				.getOrganizationId();
	}

	/** Load a user and confirm it belongs to the administrator's organization (else 404). */
	private User loadInOrg(UUID adminId, UUID userId) {
		UUID organizationId = organizationOf(adminId);
		User user = userRepository.findWithRolesById(userId)
				.orElseThrow(() -> new NotFoundException("User not found."));
		if (!organizationId.equals(user.getOrganizationId())) {
			throw new NotFoundException("User not found.");
		}
		return user;
	}

	private static ApiException selfDeactivation() {
		return new ApiException(HttpStatus.CONFLICT, "CANNOT_DEACTIVATE_SELF",
				"You cannot deactivate your own account.");
	}

	/** Derive up to two uppercase initials from a full name (fallback for a blank name). */
	private static String initialsOf(String fullName) {
		if (!StringUtils.hasText(fullName)) {
			return "?";
		}
		String[] parts = fullName.trim().split("\\s+");
		StringBuilder initials = new StringBuilder();
		for (int i = 0; i < parts.length && initials.length() < 2; i++) {
			if (!parts[i].isEmpty()) {
				initials.append(Character.toUpperCase(parts[i].charAt(0)));
			}
		}
		return initials.length() == 0 ? "?" : initials.toString();
	}
}
