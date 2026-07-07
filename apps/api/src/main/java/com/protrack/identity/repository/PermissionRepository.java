package com.protrack.identity.repository;

import com.protrack.identity.domain.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for the {@link Permission} reference set (populated as fine-grained RBAC grows). */
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

	List<Permission> findAllByOrderByCodeAsc();
}
