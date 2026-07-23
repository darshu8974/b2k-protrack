package com.protrack.assistant.service;

import com.protrack.ai.client.AiServiceClient;
import com.protrack.ai.client.AiServiceException;
import com.protrack.ai.client.AiServiceUnavailableException;
import com.protrack.ai.client.dto.AssistantChatRequest;
import com.protrack.ai.client.dto.AssistantChatRequest.HistoryMessage;
import com.protrack.ai.client.dto.AssistantChatRequest.ProjectContextDto;
import com.protrack.ai.client.dto.AssistantChatResponse;
import com.protrack.assistant.domain.AssistantMessage;
import com.protrack.assistant.domain.AssistantThread;
import com.protrack.assistant.domain.MessageRole;
import com.protrack.assistant.repository.AssistantMessageRepository;
import com.protrack.assistant.repository.AssistantThreadRepository;
import com.protrack.assistant.web.dto.AssistantMessageResponse;
import com.protrack.assistant.web.dto.AssistantThreadResponse;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectContextInfo;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.security.AuthorizationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The scoped AI Assistant: a per-(project, user) chat backed by the FastAPI AI service. Lives in its
 * own module (Backend Architecture §module map: {@code assistant → ai}) and reaches the project only
 * through {@link ProjectFacade} (existence + context + membership) — no cross-module entity access.
 *
 * <p>A turn is synchronous but must not hold a DB transaction open across the AI network call, so it
 * follows the {@link com.protrack.ai.service.AiJobWorker} idiom: a short tx persists the question and
 * gathers context, the AI service is called <em>outside</em> any transaction, then a short tx
 * persists the reply. Authorization mirrors comments: project member OR ADMIN.
 */
@Service
public class AssistantService {

	private static final String ROLE_ADMIN = "ADMIN";
	/** Cap the history sent upstream so the prompt stays bounded (most recent turns). */
	private static final int MAX_HISTORY = 20;

	private final AssistantThreadRepository threadRepository;
	private final AssistantMessageRepository messageRepository;
	private final ProjectFacade projectFacade;
	private final AuthorizationService authorizationService;
	private final AiServiceClient aiServiceClient;
	private final TransactionTemplate txTemplate;

	public AssistantService(AssistantThreadRepository threadRepository,
			AssistantMessageRepository messageRepository, ProjectFacade projectFacade,
			AuthorizationService authorizationService, AiServiceClient aiServiceClient,
			PlatformTransactionManager txManager) {
		this.threadRepository = threadRepository;
		this.messageRepository = messageRepository;
		this.projectFacade = projectFacade;
		this.authorizationService = authorizationService;
		this.aiServiceClient = aiServiceClient;
		this.txTemplate = new TransactionTemplate(txManager);
	}

	/** The current user's thread for a project (empty if no conversation has started). */
	@Transactional(readOnly = true)
	public AssistantThreadResponse getThread(UUID currentUserId, UUID projectId) {
		assertAccess(projectId, currentUserId);
		return threadRepository.findByProjectIdAndUserId(projectId, currentUserId)
				.map(thread -> new AssistantThreadResponse(thread.getId().toString(),
						messageRepository.findByThreadIdOrderByCreatedAtAsc(thread.getId()).stream()
								.map(AssistantMessageResponse::from)
								.toList()))
				.orElseGet(AssistantThreadResponse::empty);
	}

	/**
	 * Ask a scoped question: gather context/history, call the AI service outside any transaction,
	 * then persist the question and reply together. Persistence is atomic — if the AI call fails,
	 * nothing is written (no orphaned question), so the client can simply retry.
	 */
	public AssistantMessageResponse ask(UUID currentUserId, UUID projectId, String content) {
		Prepared prepared = txTemplate.execute(status -> prepare(currentUserId, projectId, content));

		AssistantChatResponse reply = callAiService(prepared.chatRequest());

		return txTemplate.execute(status -> persistTurn(prepared, reply));
	}

	// ── turn steps ───────────────────────────────────────────────────────────

	/** tx1 (read): validate access, resolve context, and snapshot prior history for the prompt. */
	private Prepared prepare(UUID currentUserId, UUID projectId, String content) {
		ProjectContextInfo context = assertAccess(projectId, currentUserId);
		// History = the turns already persisted (most recent MAX_HISTORY); the current question is
		// sent separately as `message`, so it is not part of history.
		List<HistoryMessage> history = threadRepository
				.findByProjectIdAndUserId(projectId, currentUserId)
				.map(thread -> recentHistory(thread.getId()))
				.orElseGet(List::of);
		AssistantChatRequest chatRequest = new AssistantChatRequest(content,
				toContextDto(context), history);
		return new Prepared(projectId, currentUserId, content, chatRequest);
	}

	/** tx2 (write): get-or-create the thread, then persist the question and reply atomically. */
	private AssistantMessageResponse persistTurn(Prepared prepared, AssistantChatResponse reply) {
		UUID threadId = resolveThread(prepared.projectId(), prepared.currentUserId()).getId();
		Instant now = Instant.now();

		messageRepository.save(new AssistantMessage(UUID.randomUUID(), threadId,
				MessageRole.USER, prepared.content(), null, null, now));

		Integer tokens = reply.usage() != null ? reply.usage().outputTokens() : null;
		AssistantMessage assistantMessage = messageRepository.save(new AssistantMessage(
				UUID.randomUUID(), threadId, MessageRole.ASSISTANT, reply.reply(), tokens, null,
				now.plusMillis(1)));
		return AssistantMessageResponse.from(assistantMessage, reply.citations());
	}

	private AssistantChatResponse callAiService(AssistantChatRequest request) {
		try {
			return aiServiceClient.assistantChat(request);
		} catch (AiServiceUnavailableException ex) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE",
					"The AI assistant is temporarily unavailable. Please try again.");
		} catch (AiServiceException ex) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "AI_ERROR",
					"The AI assistant could not answer this question.");
		}
	}

	// ── helpers ────────────────────────────────────────────────────────────────

	private ProjectContextInfo assertAccess(UUID projectId, UUID currentUserId) {
		ProjectContextInfo context = projectFacade.findContext(projectId)
				.orElseThrow(() -> new NotFoundException("Project not found."));
		if (!authorizationService.hasAnyRole(ROLE_ADMIN)
				&& !projectFacade.findMemberUserIds(projectId).contains(currentUserId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
					"You must be a member of this project to use the assistant.");
		}
		return context;
	}

	private AssistantThread resolveThread(UUID projectId, UUID currentUserId) {
		return threadRepository.findByProjectIdAndUserId(projectId, currentUserId)
				.orElseGet(() -> threadRepository.save(new AssistantThread(
						UUID.randomUUID(), projectId, currentUserId, Instant.now())));
	}

	private List<HistoryMessage> recentHistory(UUID threadId) {
		List<AssistantMessage> messages = messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
		int from = Math.max(0, messages.size() - MAX_HISTORY);
		return messages.subList(from, messages.size()).stream()
				.map(m -> new HistoryMessage(m.getRole().toLowerCase(), m.getContent()))
				.toList();
	}

	private static ProjectContextDto toContextDto(ProjectContextInfo context) {
		return new ProjectContextDto(context.projectId().toString(), context.title(),
				context.publicationType(), context.discipline(), context.currentStage());
	}

	/** A prepared turn: the scope, the raw question, and the chat request to send upstream. */
	private record Prepared(UUID projectId, UUID currentUserId, String content,
			AssistantChatRequest chatRequest) {
	}
}
