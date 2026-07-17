package com.protrack.qa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.preflight.spi.PreflightFacade;
import com.protrack.preflight.spi.PreflightFacade.IssueRef;
import com.protrack.preflight.spi.PreflightFacade.RunRef;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.qa.domain.Approval;
import com.protrack.qa.domain.QaSignoff;
import com.protrack.qa.repository.ApprovalRepository;
import com.protrack.qa.repository.QaIssueDecisionRepository;
import com.protrack.qa.repository.QaSignoffRepository;
import com.protrack.qa.web.dto.SignoffRequest;
import com.protrack.qa.web.dto.SignoffResponse;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.events.QaEvents;
import com.protrack.workflow.service.WorkflowService;
import com.protrack.workflow.web.dto.TransitionResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Unit tests for {@link SignoffService} — the atomic sign-off gate (Mockito; no Spring context, no
 * Docker). Focus: the "all HIGH issues triaged" 409 gate and the workflow transition per decision.
 */
class SignoffServiceTest {

	private QaSignoffRepository signoffRepository;
	private ApprovalRepository approvalRepository;
	private QaIssueDecisionRepository decisionRepository;
	private PreflightFacade preflightFacade;
	private WorkflowService workflowService;
	private ApplicationEventPublisher eventPublisher;
	private SignoffService service;

	private final UUID actor = UUID.randomUUID();
	private final UUID projectId = UUID.randomUUID();
	private final UUID runId = UUID.randomUUID();
	private final UUID highIssueId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		signoffRepository = mock(QaSignoffRepository.class);
		approvalRepository = mock(ApprovalRepository.class);
		decisionRepository = mock(QaIssueDecisionRepository.class);
		preflightFacade = mock(PreflightFacade.class);
		workflowService = mock(WorkflowService.class);
		ProjectFacade projectFacade = mock(ProjectFacade.class);
		IdentityFacade identityFacade = mock(IdentityFacade.class);
		eventPublisher = mock(ApplicationEventPublisher.class);
		service = new SignoffService(signoffRepository, approvalRepository, decisionRepository,
				preflightFacade, workflowService, projectFacade, identityFacade, eventPublisher);

		when(projectFacade.findContext(any())).thenReturn(Optional.empty());
		when(identityFacade.findBrief(any())).thenReturn(Optional.empty());
		when(signoffRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(approvalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(preflightFacade.findLatestRun(projectId))
				.thenReturn(Optional.of(new RunRef(runId, projectId, 82, true)));
	}

	private void hasHighIssue() {
		when(preflightFacade.findIssuesBySeverity(projectId, "HIGH")).thenReturn(List.of(new IssueRef(
				highIssueId, projectId, runId, "fonts", "HIGH", "Fonts not embedded", "Embed.",
				null, "AI", "OPEN", Instant.now())));
	}

	private void transitionReturns(String toStage) {
		when(workflowService.transition(eq(actor), eq(projectId), eq(toStage), any()))
				.thenReturn(new TransitionResponse(
						projectId.toString(), "QA_SIGNOFF", toStage, "QA", true, Instant.now()));
	}

	@Test
	void approveWithUntriagedHighIssueIsBlocked() {
		hasHighIssue();
		when(decisionRepository.findDecidedIssueIds(any())).thenReturn(Set.of()); // none decided

		assertThatThrownBy(() -> service.signOff(actor, projectId,
				new SignoffRequest("APPROVED", 90, "QA Approver", null)))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("HIGH_ISSUES_UNTRIAGED"));

		// Nothing is written and the project is not transitioned.
		verify(signoffRepository, never()).save(any());
		verify(approvalRepository, never()).save(any());
		verify(workflowService, never()).transition(any(), any(), any(), any());
	}

	@Test
	void approveWithAllHighTriagedSignsOffAndCompletes() {
		hasHighIssue();
		when(decisionRepository.findDecidedIssueIds(any())).thenReturn(Set.of(highIssueId));
		transitionReturns("COMPLETED");

		SignoffResponse response = service.signOff(actor, projectId,
				new SignoffRequest("APPROVED", 95, "QA Approver", "All resolved."));

		assertThat(response.decision()).isEqualTo("APPROVED");
		assertThat(response.stage()).isEqualTo("COMPLETED");
		assertThat(response.signatureHash()).isNotBlank();
		verify(signoffRepository).save(any(QaSignoff.class));
		verify(approvalRepository).save(any(Approval.class));
		verify(workflowService).transition(eq(actor), eq(projectId), eq("COMPLETED"), any());
		verify(eventPublisher).publishEvent(any(QaEvents.QaSignedOff.class));
	}

	@Test
	void rejectSkipsTheGateAndSendsBackToProduction() {
		transitionReturns("IN_PRODUCTION");

		SignoffResponse response = service.signOff(actor, projectId,
				new SignoffRequest("REJECTED", 40, "QA Approver", "Needs rework."));

		assertThat(response.stage()).isEqualTo("IN_PRODUCTION");
		verify(workflowService).transition(eq(actor), eq(projectId), eq("IN_PRODUCTION"), any());
		// The HIGH-issue gate is not evaluated for a rejection.
		verify(preflightFacade, never()).findIssuesBySeverity(any(), any());
	}

	@Test
	void signOffWithoutAPreflightRunIsRejected() {
		when(preflightFacade.findLatestRun(projectId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.signOff(actor, projectId,
				new SignoffRequest("APPROVED", 90, "QA Approver", null)))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("NO_PREFLIGHT"));
	}

	@Test
	void invalidDecisionIsRejected() {
		assertThatThrownBy(() -> service.signOff(actor, projectId,
				new SignoffRequest("MAYBE", 90, "QA Approver", null)))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("INVALID_DECISION"));
	}
}
