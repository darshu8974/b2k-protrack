package com.protrack.project.spi;

import java.util.Optional;
import java.util.UUID;

/**
 * Published interface of the project module, so the workflow module can read and advance a
 * project's stage without depending on the project entity directly.
 */
public interface ProjectFacade {

	/** Minimal stage/scope info needed to validate a transition. */
	record ProjectStageInfo(UUID projectId, String currentStage, UUID organizationId, UUID ownerId) {
	}

	Optional<ProjectStageInfo> findStageInfo(UUID projectId);

	/** Move a project to a new stage (and mark it COMPLETED when that stage is reached). */
	void updateCurrentStage(UUID projectId, String newStage, UUID actedBy);
}
