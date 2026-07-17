package com.protrack.qa.web;

import com.protrack.preflight.spi.PreflightFacade;
import com.protrack.qa.service.IssueDecisionService;
import com.protrack.qa.service.SignoffService;
import com.protrack.qa.web.dto.ApprovalResponse;
import com.protrack.qa.web.dto.BulkDecisionRequest;
import com.protrack.qa.web.dto.BulkDecisionResponse;
import com.protrack.qa.web.dto.IssueDecisionRequest;
import com.protrack.qa.web.dto.IssueDecisionResponse;
import com.protrack.qa.web.dto.IssueResponse;
import com.protrack.qa.web.dto.SignoffRequest;
import com.protrack.qa.web.dto.SignoffResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The QC/QA triage surface: list preflight issues, record per-issue and bulk decisions (QC review of
 * the paginator's work), and the formal QA sign-off (with its approval history). Reads issues via the
 * {@link PreflightFacade}; QC approves/rejects issues, QA performs the atomic completion sign-off.
 */
@RestController
@RequestMapping("/api/v1")
public class QaController {

	private final PreflightFacade preflightFacade;
	private final IssueDecisionService issueDecisionService;
	private final SignoffService signoffService;

	public QaController(PreflightFacade preflightFacade, IssueDecisionService issueDecisionService,
			SignoffService signoffService) {
		this.preflightFacade = preflightFacade;
		this.issueDecisionService = issueDecisionService;
		this.signoffService = signoffService;
	}

	@GetMapping("/projects/{projectId}/issues")
	@PreAuthorize("hasAnyRole('QC', 'QA', 'PROJECT_MANAGER', 'ADMIN')")
	public List<IssueResponse> issues(@PathVariable UUID projectId,
			@RequestParam(name = "severity", required = false) String severity,
			@RequestParam(name = "status", required = false) String status) {
		return preflightFacade.findIssues(projectId, severity, status).stream()
				.map(IssueResponse::from).toList();
	}

	@PostMapping("/issues/{issueId}/decision")
	@PreAuthorize("hasAnyRole('QC', 'ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public IssueDecisionResponse decide(@PathVariable UUID issueId,
			@Valid @RequestBody IssueDecisionRequest request, Principal principal) {
		return issueDecisionService.decide(
				currentUserId(principal), issueId, request.decision(), request.comment());
	}

	@PostMapping("/issues:bulk-decision")
	@PreAuthorize("hasAnyRole('QC', 'ADMIN')")
	public BulkDecisionResponse bulkDecide(@Valid @RequestBody BulkDecisionRequest request,
			Principal principal) {
		return issueDecisionService.bulkDecide(
				currentUserId(principal), request.issueIds(), request.decision());
	}

	@PostMapping("/projects/{projectId}/signoff")
	@PreAuthorize("hasAnyRole('QA', 'ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public SignoffResponse signOff(@PathVariable UUID projectId,
			@Valid @RequestBody SignoffRequest request, Principal principal) {
		return signoffService.signOff(currentUserId(principal), projectId, request);
	}

	@GetMapping("/projects/{projectId}/approvals")
	public List<ApprovalResponse> approvals(@PathVariable UUID projectId) {
		return signoffService.listApprovals(projectId);
	}

	@GetMapping("/projects/{projectId}/signoffs")
	public List<SignoffResponse> signoffs(@PathVariable UUID projectId) {
		return signoffService.listSignoffs(projectId);
	}

	private static UUID currentUserId(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
