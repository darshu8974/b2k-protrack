package com.protrack.workflow.repository;

import com.protrack.workflow.domain.WorkflowStage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link WorkflowStage} reference data. */
public interface WorkflowStageRepository extends JpaRepository<WorkflowStage, Integer> {

	List<WorkflowStage> findAllByOrderBySequenceAsc();
}
