package com.protrack.project.web;

import com.protrack.project.service.ProjectService;
import com.protrack.project.web.dto.AssignMembersRequest;
import com.protrack.project.web.dto.CreateProjectRequest;
import com.protrack.project.web.dto.ProjectMemberResponse;
import com.protrack.project.web.dto.ProjectResponse;
import com.protrack.project.web.dto.ProjectSummaryResponse;
import com.protrack.project.web.dto.UpdateProjectRequest;
import com.protrack.shared.web.PageResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Project management endpoints. Workflow transitions are a later task. */
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

	private final ProjectService projectService;

	public ProjectController(ProjectService projectService) {
		this.projectService = projectService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('PM', 'ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request, Principal principal) {
		return projectService.create(currentUserId(principal), request);
	}

	@GetMapping("/{id}")
	public ProjectResponse get(@PathVariable UUID id) {
		return projectService.get(id);
	}

	@PatchMapping("/{id}")
	@PreAuthorize("hasAnyRole('PM', 'ADMIN')")
	public ProjectResponse update(@PathVariable UUID id,
			@Valid @RequestBody UpdateProjectRequest request, Principal principal) {
		return projectService.update(currentUserId(principal), id, request);
	}

	@GetMapping
	public PageResponse<ProjectSummaryResponse> list(
			@RequestParam(required = false) String stage,
			@RequestParam(required = false) UUID imprintId,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String priority,
			@RequestParam(defaultValue = "false") boolean mine,
			@RequestParam(required = false) String q,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
			Principal principal) {
		return projectService.list(currentUserId(principal), stage, imprintId, status, priority,
				mine, q, pageable);
	}

	@PostMapping("/{id}/members")
	@PreAuthorize("hasAnyRole('PM', 'ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public List<ProjectMemberResponse> assignMembers(@PathVariable UUID id,
			@Valid @RequestBody AssignMembersRequest request, Principal principal) {
		return projectService.assignMembers(currentUserId(principal), id, request);
	}

	private static UUID currentUserId(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
