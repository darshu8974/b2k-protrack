package com.protrack.identity.service;

import com.protrack.identity.domain.Role;
import com.protrack.identity.repository.UserRepository;
import com.protrack.identity.web.dto.AdminUserSummary;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-side user operations (admin user directory). */
@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/** List all users with their roles (administrator view). */
	@Transactional(readOnly = true)
	public List<AdminUserSummary> listUsers() {
		return userRepository.findAllByOrderByFullNameAsc().stream()
				.map(user -> new AdminUserSummary(
						user.getId().toString(),
						user.getEmail(),
						user.getFullName(),
						user.getStatus(),
						user.getRoles().stream().map(Role::getCode).sorted().toList()))
				.toList();
	}
}
