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
import com.protrack.project.spi.ProjectFacade;
import com.protrack.qa.domain.QaIssueDecision;
import com.protrack.qa.repository.QaIssueDecisionRepository;
import com.protrack.qa.web.dto.IssueDecisionResponse;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.events.QaEvents;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/** Unit tests for {@link IssueDecisionService} (Mockito; no Spring context, no Docker). */
class IssueDecisionServiceTest {

	private QaIssueDecisionRepository decisionRepository;
	private PreflightFacade preflightFacade;
	private ApplicationEventPublisher eventPublisher;
	private IssueDecisionService service;

	private final UUID actor = UUID.randomUUID();
	private final UUID issueId = UUID.randomUUID();
	private final UUID projectId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		decisionRepository = mock(QaIssueDecisionRepository.class);
		preflightFacade = mock(PreflightFacade.class);
		ProjectFacade projectFacade = mock(ProjectFacade.class);
		IdentityFacade identityFacade = mock(IdentityFacade.class);
		eventPublisher = mock(ApplicationEventPublisher.class);
		service = new IssueDecisionService(
				decisionRepository, preflightFacade, projectFacade, identityFacade, eventPublisher);

		when(projectFacade.findContext(any())).thenReturn(Optional.empty());
		when(identityFacade.findBrief(any())).thenReturn(Optional.empty());
		when(decisionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
	}

	private void issueExists() {
		when(preflightFacade.findIssue(issueId)).thenReturn(Optional.of(new IssueRef(
				issueId, projectId, UUID.randomUUID(), "fonts", "HIGH", "Fonts not embedded",
				"Embed fonts.", null, "AI", "OPEN", Instant.now())));
	}

	@Test
	void acceptFixRecordsDecisionAndResolvesIssue() {
		issueExists();

		IssueDecisionResponse response = service.decide(actor, issueId, "ACCEPT_FIX", null);

		assertThat(response.decision()).isEqualTo("ACCEPT_FIX");
		assertThat(response.issueStatus()).isEqualTo("RESOLVED");
		verify(decisionRepository).save(any(QaIssueDecision.class));
		verify(preflightFacade).applyIssueStatus(issueId, "RESOLVED");
		verify(eventPublisher).publishEvent(any(QaEvents.IssueDecided.class));
	}

	@Test
	void sendBackWithoutCommentIsRejectedBeforeAnyWrite() {
		issueExists();

		assertThatThrownBy(() -> service.decide(actor, issueId, "SEND_BACK", "  "))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("COMMENT_REQUIRED"));

		verify(decisionRepository, never()).save(any());
		verify(preflightFacade, never()).applyIssueStatus(any(), any());
	}

	@Test
	void sendBackWithCommentTriagesTheIssue() {
		issueExists();

		IssueDecisionResponse response = service.decide(actor, issueId, "SEND_BACK", "Please fix.");

		assertThat(response.issueStatus()).isEqualTo("TRIAGED");
		verify(preflightFacade).applyIssueStatus(eq(issueId), eq("TRIAGED"));
	}

	@Test
	void invalidDecisionIsRejected() {
		issueExists();

		assertThatThrownBy(() -> service.decide(actor, issueId, "MAYBE", null))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("INVALID_DECISION"));
	}

	@Test
	void missingIssueThrowsNotFound() {
		when(preflightFacade.findIssue(issueId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.decide(actor, issueId, "ACCEPT_FIX", null))
				.isInstanceOf(NotFoundException.class);
	}
}
