package com.protrack.reporting.repository;

import com.protrack.reporting.domain.ReportSnapshot;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence for {@link ReportSnapshot} plus the reporting module's read-only <em>native</em>
 * aggregate queries. Reporting is the analytics boundary the Backend Architecture explicitly grants
 * "native aggregate queries" over the operational tables (projects, project_stage_history,
 * analysis_results, qa_signoffs, imprints); every query is org-scoped and never mutates. Column
 * aliases are quoted so they match the projection getters exactly.
 */
public interface ReportSnapshotRepository extends JpaRepository<ReportSnapshot, UUID> {

	Optional<ReportSnapshot> findByOrganizationIdAndMetricKeyAndDimensionAndPeriodStart(
			UUID organizationId, String metricKey, String dimension, LocalDate periodStart);

	List<ReportSnapshot> findByOrganizationIdAndMetricKeyOrderByPeriodStartAsc(
			UUID organizationId, String metricKey);

	/** Organizations that have (non-deleted) projects — the scope the snapshot job iterates. */
	@Query(value = "SELECT DISTINCT organization_id FROM projects WHERE deleted_at IS NULL",
			nativeQuery = true)
	List<UUID> findOrganizationIdsWithProjects();

	// ── live aggregates (operational tables) ────────────────────────────────────

	/** Average days from project creation to its first COMPLETED transition, in the window. */
	@Query(value = """
			SELECT AVG(EXTRACT(EPOCH FROM (c.completed_at - p.created_at)) / 86400.0)
			FROM projects p
			JOIN (SELECT project_id, MIN(occurred_at) AS completed_at
			      FROM project_stage_history WHERE to_stage = 'COMPLETED'
			      GROUP BY project_id) c ON c.project_id = p.id
			WHERE p.organization_id = :org AND p.deleted_at IS NULL AND c.completed_at >= :since
			""", nativeQuery = true)
	Double avgTurnaroundDays(@Param("org") UUID organizationId, @Param("since") Instant since);

	/** Projects that reached their first COMPLETED transition within the window. */
	@Query(value = """
			SELECT COUNT(*)
			FROM projects p
			JOIN (SELECT project_id, MIN(occurred_at) AS completed_at
			      FROM project_stage_history WHERE to_stage = 'COMPLETED'
			      GROUP BY project_id) c ON c.project_id = p.id
			WHERE p.organization_id = :org AND p.deleted_at IS NULL AND c.completed_at >= :since
			""", nativeQuery = true)
	long countCompleted(@Param("org") UUID organizationId, @Param("since") Instant since);

	/** Completed-in-window projects that carry a due date (the on-time denominator). */
	@Query(value = """
			SELECT COUNT(*)
			FROM projects p
			JOIN (SELECT project_id, MIN(occurred_at) AS completed_at
			      FROM project_stage_history WHERE to_stage = 'COMPLETED'
			      GROUP BY project_id) c ON c.project_id = p.id
			WHERE p.organization_id = :org AND p.deleted_at IS NULL
			  AND c.completed_at >= :since AND p.due_date IS NOT NULL
			""", nativeQuery = true)
	long countCompletedWithDueDate(@Param("org") UUID organizationId, @Param("since") Instant since);

	/** Of those, the ones completed on or before their due date (the on-time numerator). */
	@Query(value = """
			SELECT COUNT(*)
			FROM projects p
			JOIN (SELECT project_id, MIN(occurred_at) AS completed_at
			      FROM project_stage_history WHERE to_stage = 'COMPLETED'
			      GROUP BY project_id) c ON c.project_id = p.id
			WHERE p.organization_id = :org AND p.deleted_at IS NULL
			  AND c.completed_at >= :since AND p.due_date IS NOT NULL
			  AND c.completed_at::date <= p.due_date
			""", nativeQuery = true)
	long countOnTime(@Param("org") UUID organizationId, @Param("since") Instant since);

	/** Average manuscript-analysis confidence in the window. */
	@Query(value = """
			SELECT AVG(ar.overall_confidence)
			FROM analysis_results ar JOIN projects p ON p.id = ar.project_id
			WHERE p.organization_id = :org AND ar.overall_confidence IS NOT NULL
			  AND ar.created_at >= :since
			""", nativeQuery = true)
	Double avgAiConfidence(@Param("org") UUID organizationId, @Param("since") Instant since);

	/** Total QA sign-offs recorded in the window (the pass-rate denominator). */
	@Query(value = """
			SELECT COUNT(*) FROM qa_signoffs s JOIN projects p ON p.id = s.project_id
			WHERE p.organization_id = :org AND s.created_at >= :since
			""", nativeQuery = true)
	long countQaSignoffs(@Param("org") UUID organizationId, @Param("since") Instant since);

	/** Approved QA sign-offs in the window (the pass-rate numerator). */
	@Query(value = """
			SELECT COUNT(*) FROM qa_signoffs s JOIN projects p ON p.id = s.project_id
			WHERE p.organization_id = :org AND s.created_at >= :since AND s.decision = 'APPROVED'
			""", nativeQuery = true)
	long countQaApproved(@Param("org") UUID organizationId, @Param("since") Instant since);

	/** COMPLETED transitions grouped by month over the window (titles completed / month). */
	@Query(value = """
			SELECT to_char(date_trunc('month', h.occurred_at), 'YYYY-MM') AS "month",
			       COUNT(*) AS "completed"
			FROM project_stage_history h JOIN projects p ON p.id = h.project_id
			WHERE p.organization_id = :org AND h.to_stage = 'COMPLETED' AND h.occurred_at >= :since
			GROUP BY 1 ORDER BY 1
			""", nativeQuery = true)
	List<ThroughputRow> throughputByMonth(@Param("org") UUID organizationId,
			@Param("since") Instant since);

	/** Active (non-deleted) projects grouped by imprint (null imprint -> UNASSIGNED). */
	@Query(value = """
			SELECT COALESCE(i.id::text, 'UNASSIGNED') AS "imprintId",
			       COALESCE(i.name, 'Unassigned') AS "imprintName",
			       COUNT(*) AS "activeProjects"
			FROM projects p LEFT JOIN imprints i ON i.id = p.imprint_id
			WHERE p.organization_id = :org AND p.deleted_at IS NULL AND p.status = 'ACTIVE'
			GROUP BY i.id, i.name ORDER BY COUNT(*) DESC, "imprintName"
			""", nativeQuery = true)
	List<ImprintWorkloadRow> workloadByImprint(@Param("org") UUID organizationId);
}
