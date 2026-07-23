package com.protrack.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectStageInfo;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.ConflictException;
import com.protrack.shared.events.ProjectEvents.ProjectStageChanged;
import com.protrack.shared.security.AuthorizationService;
import com.protrack.workflow.repository.ProjectStageHistoryRepository;
import com.protrack.workflow.repository.WorkflowStageRepository;
import com.protrack.workflow.web.dto.TransitionResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Unit tests for {@link WorkflowService} (Mockito; no Docker/Spring context). Covers both the
 * user-triggered {@link WorkflowService#transition} (RBAC-checked) and the system-triggered
 * {@link WorkflowService#advanceIfValid} added to fix a real bug found in manual end-to-end
 * testing: completing AI analysis never advanced the project past INTAKE, because nothing called
 * the (existing, already-defined) INTAKE→AI_ANALYSIS transition. {@code advanceIfValid} is the
 * background-job-safe path — it must never throw for "already moved on," since it runs on an
 * async worker thread with no authenticated principal to check RBAC against.
 */
class WorkflowServiceTest {

	private ProjectFacade projectFacade;
	private AuthorizationService authorizationService;
	private ProjectStageHistoryRepository historyRepository;
	private ApplicationEventPublisher eventPublisher;
	private WorkflowService service;

	private final UUID projectId = UUID.randomUUID();
	private final UUID orgId = UUID.randomUUID();
	private final UUID actor = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		projectFacade = mock(ProjectFacade.class);
		IdentityFacade identityFacade = mock(IdentityFacade.class);
		authorizationService = mock(AuthorizationService.class);
		historyRepository = mock(ProjectStageHistoryRepository.class);
		WorkflowStageRepository workflowStageRepository = mock(WorkflowStageRepository.class);
		eventPublisher = mock(ApplicationEventPublisher.class);
		service = new WorkflowService(projectFacade, identityFacade, authorizationService,
				historyRepository, workflowStageRepository, eventPublisher);

		when(historyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
	}

	private void projectAt(String stage) {
		when(projectFacade.findStageInfo(projectId))
				.thenReturn(Optional.of(new ProjectStageInfo(projectId, stage, orgId, actor)));
	}

	// ── advanceIfValid (system-triggered, no RBAC check) ──────────────────────────────────────

	@Test
	void advanceIfValidMovesTheStageWhenTheTransitionIsCurrentlyValid() {
		projectAt("INTAKE");

		service.advanceIfValid(projectId, actor, "AI_ANALYSIS", "AI analysis completed.");

		verify(projectFacade).updateCurrentStage(projectId, "AI_ANALYSIS", actor);
		verify(historyRepository).save(any());
		verify(eventPublisher).publishEvent(any(ProjectStageChanged.class));
		// No RBAC call at all — this is the whole point of the system-triggered path.
		verify(authorizationService, never()).hasAnyRole(any());
	}

	@Test
	void advanceIfValidRecordsSystemAsTheTriggeredRole() {
		projectAt("INTAKE");

		service.advanceIfValid(projectId, actor, "AI_ANALYSIS", "note");

		verify(historyRepository).save(org.mockito.ArgumentMatchers.argThat(
				history -> "SYSTEM".equals(history.getTriggeredRole())));
	}

	@Test
	void advanceIfValidIsANoOpWhenTheProjectHasAlreadyMovedPastTheExpectedStage() {
		// The job started while the project was in INTAKE, but by the time it finishes (or if it's
		// a stale re-run), the PM has already manually moved it further — must not throw or revert.
		projectAt("DESIGN_PREP");

		service.advanceIfValid(projectId, actor, "AI_ANALYSIS", "note");

		verify(projectFacade, never()).updateCurrentStage(any(), any(), any());
		verify(historyRepository, never()).save(any());
		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	void advanceIfValidIsANoOpWhenTheProjectNoLongerExists() {
		when(projectFacade.findStageInfo(projectId)).thenReturn(Optional.empty());

		service.advanceIfValid(projectId, actor, "AI_ANALYSIS", "note");

		verify(projectFacade, never()).updateCurrentStage(any(), any(), any());
		verify(eventPublisher, never()).publishEvent(any());
	}

	// ── transition (user-triggered, RBAC-checked) — regression coverage after the refactor ────

	@Test
	void transitionAppliesForAnAuthorizedRole() {
		projectAt("INTAKE");
		// The allowed-roles set (rule roles + ADMIN) is built from a HashSet, so its varargs
		// iteration order isn't guaranteed — match any role array rather than a literal order.
		when(authorizationService.hasAnyRole(any(String[].class))).thenReturn(true);
		when(authorizationService.currentRoles()).thenReturn(java.util.Set.of("PROJECT_MANAGER"));

		TransitionResponse response = service.transition(actor, projectId, "AI_ANALYSIS", "go");

		assertThat(response.toStage()).isEqualTo("AI_ANALYSIS");
		assertThat(response.triggeredRole()).isEqualTo("PROJECT_MANAGER");
		verify(projectFacade).updateCurrentStage(projectId, "AI_ANALYSIS", actor);
	}

	@Test
	void transitionRejectsAnUnauthorizedRole() {
		projectAt("INTAKE");
		when(authorizationService.hasAnyRole(any(String[].class))).thenReturn(false);

		assertThatThrownBy(() -> service.transition(actor, projectId, "AI_ANALYSIS", null))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("FORBIDDEN"));
		verify(projectFacade, never()).updateCurrentStage(any(), any(), any());
	}

	@Test
	void transitionRejectsAnInvalidStagePair() {
		projectAt("INTAKE");

		assertThatThrownBy(() -> service.transition(actor, projectId, "COMPLETED", null))
				.isInstanceOfSatisfying(ConflictException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("INVALID_TRANSITION"));
	}
}
