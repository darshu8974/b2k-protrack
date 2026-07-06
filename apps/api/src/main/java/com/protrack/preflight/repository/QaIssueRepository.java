package com.protrack.preflight.repository;

import com.protrack.preflight.domain.QaIssue;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for {@link QaIssue}. */
public interface QaIssueRepository extends JpaRepository<QaIssue, UUID> {

	List<QaIssue> findByPreflightRunIdOrderByCreatedAtDesc(UUID preflightRunId);

	/** Optionally filter by severity and/or status (null = no filter); newest first. */
	@Query("""
			SELECT i FROM QaIssue i
			WHERE i.projectId = :projectId
			  AND (:severity IS NULL OR i.severity = :severity)
			  AND (:status IS NULL OR i.status = :status)
			ORDER BY i.createdAt DESC""")
	List<QaIssue> findFiltered(@Param("projectId") UUID projectId,
			@Param("severity") String severity, @Param("status") String status);

	List<QaIssue> findByProjectIdAndSeverity(UUID projectId, String severity);
}
