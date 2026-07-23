package com.protrack.workflow.service;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectStageInfo;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.ConflictException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.events.ProjectEvents;
import com.protrack.shared.security.AuthorizationService;
import com.protrack.workflow.domain.ProjectStageHistory;
import com.protrack.workflow.domain.StageTransition;
import com.protrack.workflow.repository.ProjectStageHistoryRepository;
import com.protrack.workflow.repository.WorkflowStageRepository;
import com.protrack.workflow.web.dto.TimelineEntryResponse;
import com.protrack.workflow.web.dto.TransitionResponse;
import com.protrack.workflow.web.dto.WorkflowStageResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The workflow engine: validates and applies guarded stage transitions, and records history. */
@Service
public class WorkflowService {

	private static final String ADMIN_ROLE = "ADMIN";

	private final ProjectFacade projectFacade;
	private final IdentityFacade identityFacade;
	private final AuthorizationService authorizationService;
	private final ProjectStageHistoryRepository historyRepository;
	private final WorkflowStageRepository workflowStageRepository;
	private final ApplicationEventPublisher eventPublisher;

	public WorkflowService(ProjectFacade projectFacade, IdentityFacade identityFacade,
			AuthorizationService authorizationService, ProjectStageHistoryRepository historyRepository,
			WorkflowStageRepository workflowStageRepository, ApplicationEventPublisher eventPublisher) {
		this.projectFacade = projectFacade;
		this.identityFacade = identityFacade;
		this.authorizationService = authorizationService;
		this.historyRepository = historyRepository;
		this.workflowStageRepository = workflowStageRepository;
		this.eventPublisher = eventPublisher;
	}

	/** Validate, authorize, apply, and record a stage transition. */
	@Transactional
	public TransitionResponse transition(UUID currentUserId, UUID projectId, String toStage, String note) {
		ProjectStageInfo info = projectFacade.findStageInfo(projectId)
				.orElseThrow(() -> new NotFoundException("Project not found."));
		String fromStage = info.currentStage();

		StageTransition rule = StageTransition.find(fromStage, toStage)
				.orElseThrow(() -> new ConflictException("INVALID_TRANSITION",
						"Cannot move from %s to %s.".formatted(fromStage, toStage)));

		// RBAC: the transition's roles, plus ADMIN as an override.
		Set<String> allowedRoles = new HashSet<>(rule.roles());
		allowedRoles.add(ADMIN_ROLE);
		if (!authorizationService.hasAnyRole(allowedRoles.toArray(String[]::new))) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
					"Your role is not permitted to perform this transition.");
		}
		String triggeredRole = authorizationService.currentRoles().stream()
				.filter(allowedRoles::contains).findFirst().orElse(ADMIN_ROLE);

		return apply(info, fromStage, toStage, currentUserId, triggeredRole, note, rule);
	}

	/**
	 * System-triggered transition for background job completions (e.g. AI analysis finishing),
	 * where there is no request-bound authenticated principal to check RBAC against — the
	 * originating action was already authorized when it started. Unlike {@link #transition}, this
	 * never throws for a stage/rule mismatch: a background completion firing after the project has
	 * already moved on (or was never at the expected stage) is a no-op, not an error.
	 */
	@Transactional
	public void advanceIfValid(UUID projectId, UUID actedBy, String toStage, String note) {
		projectFacade.findStageInfo(projectId).ifPresent(info -> {
			String fromStage = info.currentStage();
			StageTransition.find(fromStage, toStage)
					.ifPresent(rule -> apply(info, fromStage, toStage, actedBy, "SYSTEM", note, rule));
		});
	}

	private TransitionResponse apply(ProjectStageInfo info, String fromStage, String toStage,
			UUID actedBy, String triggeredRole, String note, StageTransition rule) {
		projectFacade.updateCurrentStage(info.projectId(), toStage, actedBy);

		ProjectStageHistory history = historyRepository.save(new ProjectStageHistory(
				UUID.randomUUID(), info.projectId(), fromStage, toStage, triggeredRole, actedBy, note));

		eventPublisher.publishEvent(new ProjectEvents.ProjectStageChanged(
				info.organizationId(), info.projectId(), actedBy, fromStage, toStage, triggeredRole));

		return new TransitionResponse(info.projectId().toString(), fromStage, toStage, triggeredRole,
				rule.approvalGate(), history.getOccurredAt());
	}

	@Transactional(readOnly = true)
	public List<TimelineEntryResponse> timeline(UUID projectId) {
		List<ProjectStageHistory> history = historyRepository.findByProjectIdOrderByOccurredAtAsc(projectId);
		Set<UUID> actorIds = history.stream()
				.map(ProjectStageHistory::getTriggeredBy).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<UUID, UserBrief> actors = identityFacade.findBriefs(actorIds);

		return history.stream().map(entry -> new TimelineEntryResponse(
				entry.getFromStage(),
				entry.getToStage(),
				entry.getTriggeredRole(),
				resolveName(actors, entry.getTriggeredBy()),
				entry.getNote(),
				entry.getOccurredAt())).toList();
	}

	@Transactional(readOnly = true)
	public List<WorkflowStageResponse> stages() {
		return workflowStageRepository.findAllByOrderBySequenceAsc().stream()
				.map(stage -> new WorkflowStageResponse(
						stage.getCode(), stage.getName(), stage.getSequence(), stage.getDescription()))
				.toList();
	}

	private static String resolveName(Map<UUID, UserBrief> actors, UUID actorId) {
		if (actorId == null) {
			return null;
		}
		UserBrief brief = actors.get(actorId);
		return brief == null ? null : brief.fullName();
	}
}
