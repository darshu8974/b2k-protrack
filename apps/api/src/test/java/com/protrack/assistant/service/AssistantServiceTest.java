package com.protrack.assistant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.protrack.ai.client.AiServiceClient;
import com.protrack.ai.client.AiServiceException;
import com.protrack.ai.client.AiServiceUnavailableException;
import com.protrack.ai.client.dto.AssistantChatResponse;
import com.protrack.assistant.domain.AssistantMessage;
import com.protrack.assistant.domain.AssistantThread;
import com.protrack.assistant.repository.AssistantMessageRepository;
import com.protrack.assistant.repository.AssistantThreadRepository;
import com.protrack.assistant.web.dto.AssistantMessageResponse;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectContextInfo;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.security.AuthorizationService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/** Unit tests for {@link AssistantService} — the Spring→AI gateway (Mockito; no Docker). */
class AssistantServiceTest {

	private AssistantThreadRepository threadRepository;
	private AssistantMessageRepository messageRepository;
	private ProjectFacade projectFacade;
	private AuthorizationService authorizationService;
	private AiServiceClient aiServiceClient;
	private AssistantService service;

	private final UUID member = UUID.randomUUID();
	private final UUID projectId = UUID.randomUUID();
	private final UUID orgId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		threadRepository = mock(AssistantThreadRepository.class);
		messageRepository = mock(AssistantMessageRepository.class);
		projectFacade = mock(ProjectFacade.class);
		authorizationService = mock(AuthorizationService.class);
		aiServiceClient = mock(AiServiceClient.class);
		PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);
		when(txManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
		service = new AssistantService(threadRepository, messageRepository, projectFacade,
				authorizationService, aiServiceClient, txManager);

		when(projectFacade.findContext(projectId)).thenReturn(Optional.of(new ProjectContextInfo(
				projectId, orgId, "Quantum Mechanics 3e", "MONOGRAPH", "Physics")));
		when(threadRepository.findByProjectIdAndUserId(projectId, member)).thenReturn(Optional.empty());
		when(threadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(messageRepository.findByThreadIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
		when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
	}

	private void memberAccess() {
		when(authorizationService.hasAnyRole("ADMIN")).thenReturn(false);
		when(projectFacade.findMemberUserIds(projectId)).thenReturn(List.of(member));
	}

	@Test
	void askPersistsQuestionAndReplyThenReturnsTheReply() {
		memberAccess();
		when(aiServiceClient.assistantChat(any())).thenReturn(new AssistantChatResponse(
				"It is in AI Analysis.", List.of("projectContext.title"),
				new AssistantChatResponse.UsageDto(10, 20, "mock")));

		AssistantMessageResponse response = service.ask(member, projectId, "What stage is this?");

		assertThat(response.role()).isEqualTo("ASSISTANT");
		assertThat(response.content()).isEqualTo("It is in AI Analysis.");
		assertThat(response.tokens()).isEqualTo(20);
		// A get-or-created thread plus a persisted question and answer.
		verify(threadRepository).save(any(AssistantThread.class));
		verify(messageRepository, times(2)).save(any(AssistantMessage.class));
	}

	@Test
	void nonMemberIsForbidden() {
		when(authorizationService.hasAnyRole("ADMIN")).thenReturn(false);
		when(projectFacade.findMemberUserIds(projectId)).thenReturn(List.of(UUID.randomUUID()));

		assertThatThrownBy(() -> service.ask(member, projectId, "hi"))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("FORBIDDEN"));
		verify(aiServiceClient, never()).assistantChat(any());
	}

	@Test
	void unknownProjectIsNotFound() {
		when(projectFacade.findContext(projectId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.ask(member, projectId, "hi"))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void aiUnavailableMapsToServiceUnavailableAndPersistsNothing() {
		memberAccess();
		when(aiServiceClient.assistantChat(any()))
				.thenThrow(new AiServiceUnavailableException("down"));

		assertThatThrownBy(() -> service.ask(member, projectId, "hi"))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("AI_UNAVAILABLE"));
		// Atomic: the reply failed, so neither the question nor a reply is written.
		verify(messageRepository, never()).save(any());
	}

	@Test
	void aiRejectionMapsToBadGateway() {
		memberAccess();
		when(aiServiceClient.assistantChat(any())).thenThrow(new AiServiceException("bad"));

		assertThatThrownBy(() -> service.ask(member, projectId, "hi"))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("AI_ERROR"));
	}
}
