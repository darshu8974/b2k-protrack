package com.protrack.qa.repository;

import com.protrack.qa.domain.QaIssueDecision;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for {@link QaIssueDecision} (append-only triage trail). */
public interface QaIssueDecisionRepository extends JpaRepository<QaIssueDecision, UUID> {

	List<QaIssueDecision> findByIssueIdOrderByCreatedAtAsc(UUID issueId);

	/** The subset of the given issue ids that have at least one decision (for the sign-off gate). */
	@Query("SELECT DISTINCT d.issueId FROM QaIssueDecision d WHERE d.issueId IN :issueIds")
	Set<UUID> findDecidedIssueIds(@Param("issueIds") Collection<UUID> issueIds);
}
