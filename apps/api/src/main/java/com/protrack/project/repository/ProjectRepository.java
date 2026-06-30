package com.protrack.project.repository;

import com.protrack.project.domain.Project;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Data access for {@link Project}, with Specification support for list filtering. */
public interface ProjectRepository
		extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

	@EntityGraph(attributePaths = {"imprint", "members"})
	Optional<Project> findWithDetailsByIdAndDeletedAtIsNull(UUID id);

	Optional<Project> findByIdAndDeletedAtIsNull(UUID id);

	boolean existsByIsbnAndDeletedAtIsNull(String isbn);

	boolean existsByIsbnAndDeletedAtIsNullAndIdNot(String isbn, UUID id);
}
