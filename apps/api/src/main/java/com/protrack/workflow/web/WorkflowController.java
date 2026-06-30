package com.protrack.workflow.web;

import com.protrack.workflow.service.WorkflowService;
import com.protrack.workflow.web.dto.TimelineEntryResponse;
import com.protrack.workflow.web.dto.TransitionRequest;
import com.protrack.workflow.web.dto.TransitionResponse;
import com.protrack.workflow.web.dto.WorkflowStageResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Workflow endpoints: perform a guarded stage transition, read a project's timeline, and list the
 * canonical stages. Transition RBAC is enforced per-rule inside the service.
 */
@RestController
@RequestMapping("/api/v1")
public class WorkflowController {

	private final WorkflowService workflowService;

	public WorkflowController(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@PostMapping("/projects/{id}/transitions")
	public TransitionResponse transition(@PathVariable UUID id,
			@Valid @RequestBody TransitionRequest request, Principal principal) {
		return workflowService.transition(
				UUID.fromString(principal.getName()), id, request.toStage(), request.note());
	}

	@GetMapping("/projects/{id}/timeline")
	public List<TimelineEntryResponse> timeline(@PathVariable UUID id) {
		return workflowService.timeline(id);
	}

	@GetMapping("/workflow-stages")
	public List<WorkflowStageResponse> stages() {
		return workflowService.stages();
	}
}
