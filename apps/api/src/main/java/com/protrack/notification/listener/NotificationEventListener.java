package com.protrack.notification.listener;

import com.protrack.notification.domain.NotificationType;
import com.protrack.notification.service.NotificationRequest;
import com.protrack.notification.service.NotificationService;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.shared.events.AiEvents;
import com.protrack.shared.events.ProjectEvents;
import com.protrack.shared.events.QaEvents;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Fans out in-app + email notifications from domain events — the cross-cutting "notification"
 * subscriber. Listens {@code AFTER_COMMIT} so recipients are only notified once the originating
 * business change is durable, and delegates to {@link NotificationService#publish} (which runs in
 * its own transaction) so a notification/email failure never rolls anything back.
 *
 * <p>Recipients are a project's members (the actor is excluded — you don't notify yourself of your
 * own action). This depends on no other module, only on published events and the project SPI.
 */
@Component
public class NotificationEventListener {

	/** Human-readable stage names (kept in sync with the frontend STAGE_LABEL). */
	private static final Map<String, String> STAGE_LABELS = Map.of(
			"INTAKE", "Intake",
			"AI_ANALYSIS", "AI Analysis",
			"DESIGN_PREP", "Design Prep",
			"IN_PRODUCTION", "In Production",
			"PDF_REVIEW", "PDF Review",
			"QA_SIGNOFF", "QA Sign-off",
			"COMPLETED", "Completed");

	private static final String ENTITY_PROJECT = "PROJECT";
	private static final String ENTITY_AI_JOB = "AI_JOB";
	private static final String ENTITY_PREFLIGHT_RUN = "PREFLIGHT_RUN";
	private static final String ENTITY_QA_SIGNOFF = "QA_SIGNOFF";

	private final NotificationService notificationService;
	private final ProjectFacade projectFacade;

	public NotificationEventListener(NotificationService notificationService,
			ProjectFacade projectFacade) {
		this.notificationService = notificationService;
		this.projectFacade = projectFacade;
	}

	@TransactionalEventListener
	public void onStageChanged(ProjectEvents.ProjectStageChanged event) {
		String stage = stageLabel(event.toStage());
		fanOut(NotificationType.STAGE_CHANGED, event.projectId(), event.actorId(),
				"Moved to " + stage,
				"%s moved to %s.".formatted(projectTitle(event.projectId()), stage),
				ENTITY_PROJECT, event.projectId());
	}

	@TransactionalEventListener
	public void onAnalysisCompleted(AiEvents.AnalysisCompleted event) {
		fanOut(NotificationType.ANALYSIS_COMPLETED, event.projectId(), event.actorId(),
				"AI analysis completed",
				"%s: AI manuscript analysis is ready%s.".formatted(projectTitle(event.projectId()),
						confidenceSuffix(event.overallConfidence())),
				ENTITY_AI_JOB, event.jobId());
	}

	@TransactionalEventListener
	public void onPreflightCompleted(AiEvents.PreflightCompleted event) {
		String outcome = Boolean.TRUE.equals(event.passed()) ? "passed" : "needs review";
		fanOut(NotificationType.PREFLIGHT_COMPLETED, event.projectId(), event.actorId(),
				"PDF preflight completed",
				"%s: PDF preflight %s (%d issue(s)).".formatted(projectTitle(event.projectId()),
						outcome, event.totalIssues() == null ? 0 : event.totalIssues()),
				ENTITY_PREFLIGHT_RUN, event.preflightRunId());
	}

	@TransactionalEventListener
	public void onQaSignedOff(QaEvents.QaSignedOff event) {
		fanOut(NotificationType.QA_SIGNED_OFF, event.projectId(), event.actorId(),
				"QA sign-off: " + event.decision(),
				"%s: QA recorded a %s sign-off.".formatted(projectTitle(event.projectId()),
						event.decision().toLowerCase()),
				ENTITY_QA_SIGNOFF, event.signoffId());
	}

	private void fanOut(NotificationType type, UUID projectId, UUID actorId, String title, String body,
			String entityType, UUID entityId) {
		List<UUID> recipients = projectFacade.findMemberUserIds(projectId).stream()
				.filter(id -> !id.equals(actorId))
				.toList();
		if (recipients.isEmpty()) {
			return;
		}
		notificationService.publish(new NotificationRequest(type, recipients, title, body, projectId,
				entityType, entityId));
	}

	private String projectTitle(UUID projectId) {
		return projectFacade.findContext(projectId)
				.map(ProjectFacade.ProjectContextInfo::title)
				.orElse("A project");
	}

	private static String stageLabel(String code) {
		return STAGE_LABELS.getOrDefault(code, code);
	}

	private static String confidenceSuffix(Integer confidence) {
		return confidence == null ? "" : " (%d%% confidence)".formatted(confidence);
	}
}
