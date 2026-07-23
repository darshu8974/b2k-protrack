package com.protrack.workflow.spi;

import com.protrack.workflow.service.WorkflowService;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Default {@link WorkflowFacade} backed by {@link WorkflowService}. */
@Service
public class WorkflowFacadeImpl implements WorkflowFacade {

	private final WorkflowService workflowService;

	public WorkflowFacadeImpl(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@Override
	public void advanceIfValid(UUID projectId, UUID actedBy, String toStage, String note) {
		workflowService.advanceIfValid(projectId, actedBy, toStage, note);
	}
}
