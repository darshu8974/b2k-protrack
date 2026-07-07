package com.protrack.identity.repository;

import com.protrack.identity.domain.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for the fixed {@link Role} reference set. */
public interface RoleRepository extends JpaRepository<Role, Integer> {

	Optional<Role> findByCode(String code);

	List<Role> findAllByOrderByCodeAsc();
}
