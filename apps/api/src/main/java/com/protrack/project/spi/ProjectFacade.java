package com.protrack.project.spi;

import java.util.List;
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

	/** Project context for scoping AI work (passed to the AI service, no entity leakage). */
	record ProjectContextInfo(UUID projectId, UUID organizationId, String title,
			String publicationType, String discipline, String currentStage) {
	}

	Optional<ProjectStageInfo> findStageInfo(UUID projectId);

	Optional<ProjectContextInfo> findContext(UUID projectId);

	/**
	 * The user ids of a project's members (the owner is included, as owners are auto-added as
	 * members). Used by cross-cutting subscribers (notifications) to fan out to a project's team
	 * without reaching into the project entity.
	 */
	List<UUID> findMemberUserIds(UUID projectId);

	/** Move a project to a new stage (and mark it COMPLETED when that stage is reached). */
	void updateCurrentStage(UUID projectId, String newStage, UUID actedBy);
}
