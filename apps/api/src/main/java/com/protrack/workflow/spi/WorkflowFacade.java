package com.protrack.workflow.spi;

import java.util.UUID;

/**
 * Published interface of the workflow module, so other modules can trigger a system-driven stage
 * advance (e.g. after a background job completes) without depending on {@code WorkflowService}
 * directly or needing a request-bound authenticated principal.
 */
public interface WorkflowFacade {

	/**
	 * Advance a project to {@code toStage} if that is currently a valid transition from its
	 * current stage; a silent no-op (not an error) otherwise — e.g. the project already moved
	 * past the expected stage, or was never at the expected "from" stage. Records history and
	 * publishes {@code ProjectStageChanged} exactly like a user-triggered transition, but skips
	 * the role check: the caller is responsible for having already authorized the action that led
	 * here (e.g. only a PROJECT_MANAGER/ADMIN can start manuscript analysis in the first place).
	 */
	void advanceIfValid(UUID projectId, UUID actedBy, String toStage, String note);
}
