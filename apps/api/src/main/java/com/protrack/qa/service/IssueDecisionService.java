package com.protrack.qa.service;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.preflight.spi.PreflightFacade;
import com.protrack.preflight.spi.PreflightFacade.IssueRef;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectContextInfo;
import com.protrack.qa.domain.DecisionType;
import com.protrack.qa.domain.QaIssueDecision;
import com.protrack.qa.repository.QaIssueDecisionRepository;
import com.protrack.qa.web.dto.BulkDecisionResponse;
import com.protrack.qa.web.dto.IssueDecisionResponse;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.events.QaEvents;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Records QA triage decisions on preflight issues. Each decision is appended to
 * {@code qa_issue_decisions} (the trail is the record) and the issue's derived status is updated via
 * the {@link PreflightFacade}. Single decisions require a comment for SEND_BACK/COMMENT; bulk triage
 * applies one decision to many issues without per-issue comments.
 */
@Service
public class IssueDecisionService {

	private final QaIssueDecisionRepository decisionRepository;
	private final PreflightFacade preflightFacade;
	private final ProjectFacade projectFacade;
	private final IdentityFacade identityFacade;
	private final ApplicationEventPublisher eventPublisher;

	public IssueDecisionService(QaIssueDecisionRepository decisionRepository,
			PreflightFacade preflightFacade, ProjectFacade projectFacade,
			IdentityFacade identityFacade, ApplicationEventPublisher eventPublisher) {
		this.decisionRepository = decisionRepository;
		this.preflightFacade = preflightFacade;
		this.projectFacade = projectFacade;
		this.identityFacade = identityFacade;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public IssueDecisionResponse decide(UUID actorId, UUID issueId, String decisionValue,
			String comment) {
		IssueRef issue = preflightFacade.findIssue(issueId)
				.orElseThrow(() -> new NotFoundException("Issue not found."));
		DecisionType decision = parseDecision(decisionValue);
		if (decision.requiresComment() && !StringUtils.hasText(comment)) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "COMMENT_REQUIRED",
					"A comment is required for a %s decision.".formatted(decision.name()));
		}

		QaIssueDecision saved = record(actorId, issue, decision, comment);
		publish(issue, actorId, decision);

		String name = identityFacade.findBrief(actorId).map(UserBrief::fullName).orElse(null);
		// created_at is a DB default (assigned on flush); report the wall-clock time for the response.
		return new IssueDecisionResponse(saved.getId().toString(), issueId.toString(),
				decision.name(), saved.getComment(), actorId.toString(), name,
				decision.resultingStatus().name(), Instant.now());
	}

	@Transactional
	public BulkDecisionResponse bulkDecide(UUID actorId, List<UUID> issueIds, String decisionValue) {
		DecisionType decision = parseDecision(decisionValue);
		List<String> decided = issueIds.stream().distinct().map(issueId -> {
			IssueRef issue = preflightFacade.findIssue(issueId)
					.orElseThrow(() -> new NotFoundException("Issue not found: " + issueId));
			record(actorId, issue, decision, null);
			publish(issue, actorId, decision);
			return issueId.toString();
		}).toList();
		return new BulkDecisionResponse(decided.size(), decision.name(),
				decision.resultingStatus().name(), decided);
	}

	private QaIssueDecision record(UUID actorId, IssueRef issue, DecisionType decision,
			String comment) {
		QaIssueDecision saved = decisionRepository.save(new QaIssueDecision(
				UUID.randomUUID(), issue.id(), actorId, decision.name(),
				StringUtils.hasText(comment) ? comment.trim() : null));
		preflightFacade.applyIssueStatus(issue.id(), decision.resultingStatus().name());
		return saved;
	}

	private void publish(IssueRef issue, UUID actorId, DecisionType decision) {
		UUID organizationId = projectFacade.findContext(issue.projectId())
				.map(ProjectContextInfo::organizationId).orElse(null);
		eventPublisher.publishEvent(new QaEvents.IssueDecided(
				organizationId, issue.projectId(), actorId, issue.id(), decision.name()));
	}

	private static DecisionType parseDecision(String value) {
		try {
			return DecisionType.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException | NullPointerException ex) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_DECISION",
					"Decision must be one of ACCEPT_FIX, SEND_BACK, COMMENT.");
		}
	}
}
