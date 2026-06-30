package com.protrack.workflow.repository;

import com.protrack.workflow.domain.ProjectStageHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for the append-only {@link ProjectStageHistory}. */
public interface ProjectStageHistoryRepository extends JpaRepository<ProjectStageHistory, UUID> {

	List<ProjectStageHistory> findByProjectIdOrderByOccurredAtAsc(UUID projectId);
}
