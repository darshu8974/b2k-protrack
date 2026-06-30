package com.protrack.identity.repository;

import com.protrack.identity.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link User}, fetching roles + permissions eagerly via entity graph. */
public interface UserRepository extends JpaRepository<User, UUID> {

	@EntityGraph(attributePaths = {"roles", "roles.permissions"})
	Optional<User> findByEmail(String email);

	@EntityGraph(attributePaths = {"roles", "roles.permissions"})
	Optional<User> findWithRolesById(UUID id);

	@EntityGraph(attributePaths = {"roles"})
	List<User> findAllByOrderByFullNameAsc();
}
