package com.protrack.project.repository;

import com.protrack.project.domain.Project;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for {@link Project}, with Specification support for list filtering. */
public interface ProjectRepository
		extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

	@EntityGraph(attributePaths = {"imprint", "members"})
	Optional<Project> findWithDetailsByIdAndDeletedAtIsNull(UUID id);

	Optional<Project> findByIdAndDeletedAtIsNull(UUID id);

	boolean existsByIsbnAndDeletedAtIsNull(String isbn);

	boolean existsByIsbnAndDeletedAtIsNullAndIdNot(String isbn, UUID id);

	// --- dashboard aggregates (single grouped queries; no entity hydration) ---

	@Query("""
			SELECT p.currentStage AS stage, COUNT(p) AS total FROM Project p
			WHERE p.organizationId = :org AND p.deletedAt IS NULL GROUP BY p.currentStage""")
	List<StageCountView> countByStage(@Param("org") UUID organizationId);

	@Query("""
			SELECT p.status AS status, COUNT(p) AS total FROM Project p
			WHERE p.organizationId = :org AND p.deletedAt IS NULL GROUP BY p.status""")
	List<StatusCountView> countByStatus(@Param("org") UUID organizationId);

	@Query("""
			SELECT COUNT(p) FROM Project p WHERE p.organizationId = :org AND p.deletedAt IS NULL
			AND p.status = 'COMPLETED' AND p.updatedAt >= :since""")
	long countCompletedSince(@Param("org") UUID organizationId, @Param("since") Instant since);

	@EntityGraph(attributePaths = {"imprint"})
	List<Project> findTop5ByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID organizationId);

	@Query("""
			SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.imprint LEFT JOIN p.members m
			WHERE p.organizationId = :org AND p.deletedAt IS NULL
			AND (p.ownerId = :userId OR m.userId = :userId)
			ORDER BY p.dueDate ASC""")
	List<Project> findAssignedTo(@Param("org") UUID organizationId, @Param("userId") UUID userId);
}
