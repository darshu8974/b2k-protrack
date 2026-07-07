package com.protrack.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.protrack.identity.domain.Role;
import com.protrack.identity.domain.User;
import com.protrack.identity.repository.PermissionRepository;
import com.protrack.identity.repository.RoleRepository;
import com.protrack.identity.repository.UserRepository;
import com.protrack.identity.web.dto.AdminUserResponse;
import com.protrack.identity.web.dto.BulkUserRequest;
import com.protrack.identity.web.dto.BulkUserResult;
import com.protrack.identity.web.dto.CreateUserRequest;
import com.protrack.shared.error.ApiException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Unit tests for {@link UserService} admin operations + self-protection guards (no Docker). */
class UserServiceTest {

	private UserRepository userRepository;
	private RoleRepository roleRepository;
	private PasswordEncoder passwordEncoder;
	private UserService service;

	private final UUID adminId = UUID.randomUUID();
	private final UUID orgId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		roleRepository = mock(RoleRepository.class);
		PermissionRepository permissionRepository = mock(PermissionRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		service = new UserService(userRepository, roleRepository, permissionRepository, passwordEncoder);

		User admin = new User(adminId, orgId, "admin@protrack.io", "hash", "Ada Admin", "AA", "#000");
		when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
		when(userRepository.findWithRolesById(adminId)).thenReturn(Optional.of(admin));
		when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
	}

	private static Role role(int id, String code) {
		Role role = mock(Role.class);
		when(role.getId()).thenReturn(id);
		when(role.getCode()).thenReturn(code);
		return role;
	}

	@Test
	void createUserEncodesPasswordAndDerivesInitials() {
		Role qa = role(2, "QA");
		when(userRepository.existsByEmail("test.user@protrack.io")).thenReturn(false);
		when(roleRepository.findById(2)).thenReturn(Optional.of(qa));
		when(passwordEncoder.encode("password123")).thenReturn("bcrypted");

		AdminUserResponse response = service.createUser(adminId, new CreateUserRequest(
				"Test.User@protrack.io", "Test User", 2, "password123", null));

		assertThat(response.email()).isEqualTo("test.user@protrack.io"); // lower-cased
		assertThat(response.roles()).containsExactly("QA");
		assertThat(response.status()).isEqualTo("ACTIVE");
		assertThat(response.avatarInitials()).isEqualTo("TU");
	}

	@Test
	void createUserWithADuplicateEmailIs409() {
		when(userRepository.existsByEmail("dup@protrack.io")).thenReturn(true);

		assertThatThrownBy(() -> service.createUser(adminId,
				new CreateUserRequest("dup@protrack.io", "Dup", 2, "password123", null)))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("DUPLICATE_EMAIL"));
		verify(userRepository, never()).save(any());
	}

	@Test
	void createUserWithAnUnknownRoleIs422() {
		when(userRepository.existsByEmail(any())).thenReturn(false);
		when(roleRepository.findById(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.createUser(adminId,
				new CreateUserRequest("x@protrack.io", "X", 99, "password123", null)))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("INVALID_ROLE"));
	}

	@Test
	void anAdminCannotDeactivateThemselves() {
		assertThatThrownBy(() -> service.deactivateUser(adminId, adminId))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("CANNOT_DEACTIVATE_SELF"));
	}

	@Test
	void revokingAUsersLastRoleIsRejected() {
		UUID targetId = UUID.randomUUID();
		Role qa = role(3, "QA");
		User target = new User(targetId, orgId, "t@protrack.io", "h", "Target", "TG", "#111");
		target.addRole(qa);
		when(userRepository.findWithRolesById(targetId)).thenReturn(Optional.of(target));
		when(roleRepository.findById(3)).thenReturn(Optional.of(qa));

		assertThatThrownBy(() -> service.revokeRole(adminId, targetId, 3))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("LAST_ROLE"));
	}

	@Test
	void bulkDeactivateSkipsTheActingAdmin() {
		UUID otherId = UUID.randomUUID();
		User other = new User(otherId, orgId, "o@protrack.io", "h", "Other", "OT", "#222");
		when(userRepository.findById(otherId)).thenReturn(Optional.of(other));

		BulkUserResult result = service.bulkUpdateStatus(adminId,
				new BulkUserRequest("DEACTIVATE", List.of(adminId, otherId)));

		assertThat(result.updated()).isEqualTo(1); // other
		assertThat(result.skipped()).isEqualTo(1); // self
		assertThat(other.getStatus()).isEqualTo("INACTIVE");
	}
}
