package com.protrack.qa.service;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.preflight.spi.PreflightFacade;
import com.protrack.preflight.spi.PreflightFacade.IssueRef;
import com.protrack.preflight.spi.PreflightFacade.RunRef;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectContextInfo;
import com.protrack.qa.domain.Approval;
import com.protrack.qa.domain.QaSignoff;
import com.protrack.qa.domain.SignoffDecision;
import com.protrack.qa.repository.ApprovalRepository;
import com.protrack.qa.repository.QaIssueDecisionRepository;
import com.protrack.qa.repository.QaSignoffRepository;
import com.protrack.qa.web.dto.ApprovalResponse;
import com.protrack.qa.web.dto.SignoffRequest;
import com.protrack.qa.web.dto.SignoffResponse;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.ConflictException;
import com.protrack.shared.events.QaEvents;
import com.protrack.workflow.service.WorkflowService;
import com.protrack.workflow.web.dto.TransitionResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
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
import org.springframework.util.StringUtils;

/**
 * The QA sign-off gate. Signing off is a single atomic transaction: validate the "all HIGH issues
 * triaged" gate (approvals only), then write the formal {@code qa_signoffs} attestation and an
 * {@code approvals} gate record, then advance the project via the workflow engine (COMPLETED on
 * APPROVED, IN_PRODUCTION on REJECTED — reusing the {@code SIGN_OFF}/{@code REJECT_FROM_QA} rules).
 * Any failure rolls the whole thing back, so a title never half-completes.
 */
@Service
public class SignoffService {

	private static final String HIGH = "HIGH";
	private static final String STAGE_CODE = "QA_SIGNOFF";
	private static final String APPROVAL_TYPE = "QA_FINAL";
	private static final String DECIDED_ROLE = "QA";

	private final QaSignoffRepository signoffRepository;
	private final ApprovalRepository approvalRepository;
	private final QaIssueDecisionRepository decisionRepository;
	private final PreflightFacade preflightFacade;
	private final WorkflowService workflowService;
	private final ProjectFacade projectFacade;
	private final IdentityFacade identityFacade;
	private final ApplicationEventPublisher eventPublisher;

	public SignoffService(QaSignoffRepository signoffRepository, ApprovalRepository approvalRepository,
			QaIssueDecisionRepository decisionRepository, PreflightFacade preflightFacade,
			WorkflowService workflowService, ProjectFacade projectFacade,
			IdentityFacade identityFacade, ApplicationEventPublisher eventPublisher) {
		this.signoffRepository = signoffRepository;
		this.approvalRepository = approvalRepository;
		this.decisionRepository = decisionRepository;
		this.preflightFacade = preflightFacade;
		this.workflowService = workflowService;
		this.projectFacade = projectFacade;
		this.identityFacade = identityFacade;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public SignoffResponse signOff(UUID actorId, UUID projectId, SignoffRequest request) {
		SignoffDecision decision = parseDecision(request.decision());

		RunRef run = preflightFacade.findLatestRun(projectId)
				.orElseThrow(() -> new ConflictException("NO_PREFLIGHT",
						"Run preflight before signing off."));

		// Gate: approval requires every HIGH-severity issue to have been triaged (have a decision).
		if (decision == SignoffDecision.APPROVED) {
			requireHighIssuesTriaged(projectId);
		}

		UUID organizationId = projectFacade.findContext(projectId)
				.map(ProjectContextInfo::organizationId).orElse(null);
		String notes = StringUtils.hasText(request.notes()) ? request.notes().trim() : null;
		String signatureHash = hash(request.signature(), actorId);

		QaSignoff signoff = signoffRepository.save(new QaSignoff(
				UUID.randomUUID(), projectId, run.id(), actorId, decision.name(),
				request.qualityScore(), signatureHash, null, notes));
		approvalRepository.save(new Approval(
				UUID.randomUUID(), projectId, STAGE_CODE, APPROVAL_TYPE, decision.name(),
				DECIDED_ROLE, actorId, notes));

		// Atomic stage transition (reuses the workflow engine; joins this transaction).
		TransitionResponse transition = workflowService.transition(
				actorId, projectId, decision.targetStage(), "QA sign-off: " + decision.name());

		eventPublisher.publishEvent(new QaEvents.QaSignedOff(
				organizationId, projectId, actorId, signoff.getId(), decision.name(),
				request.qualityScore(), transition.toStage()));

		String name = identityFacade.findBrief(actorId).map(UserBrief::fullName).orElse(null);
		return new SignoffResponse(signoff.getId().toString(), projectId.toString(),
				run.id().toString(), decision.name(), request.qualityScore(), signatureHash, notes,
				actorId.toString(), name, transition.toStage(), Instant.now());
	}

	@Transactional(readOnly = true)
	public List<SignoffResponse> listSignoffs(UUID projectId) {
		List<QaSignoff> signoffs = signoffRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
		Map<UUID, UserBrief> actors = resolveActors(signoffs.stream().map(QaSignoff::getSignedBy));
		return signoffs.stream().map(signoff -> new SignoffResponse(
				signoff.getId().toString(), signoff.getProjectId().toString(),
				signoff.getPreflightRunId().toString(), signoff.getDecision(),
				signoff.getQualityScore(), signoff.getSignatureHash(), signoff.getNotes(),
				idString(signoff.getSignedBy()), nameOf(actors, signoff.getSignedBy()), null,
				signoff.getCreatedAt())).toList();
	}

	@Transactional(readOnly = true)
	public List<ApprovalResponse> listApprovals(UUID projectId) {
		List<Approval> approvals = approvalRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
		Map<UUID, UserBrief> actors = resolveActors(approvals.stream().map(Approval::getDecidedBy));
		return approvals.stream().map(approval -> new ApprovalResponse(
				approval.getId().toString(), approval.getProjectId().toString(),
				approval.getStageCode(), approval.getApprovalType(), approval.getDecision(),
				approval.getDecidedRole(), idString(approval.getDecidedBy()),
				nameOf(actors, approval.getDecidedBy()), approval.getComment(),
				approval.getCreatedAt())).toList();
	}

	private void requireHighIssuesTriaged(UUID projectId) {
		List<IssueRef> highIssues = preflightFacade.findIssuesBySeverity(projectId, HIGH);
		if (highIssues.isEmpty()) {
			return;
		}
		Set<UUID> highIds = highIssues.stream().map(IssueRef::id).collect(Collectors.toSet());
		Set<UUID> decided = decisionRepository.findDecidedIssueIds(highIds);
		long undecided = highIds.stream().filter(id -> !decided.contains(id)).count();
		if (undecided > 0) {
			throw new ConflictException("HIGH_ISSUES_UNTRIAGED",
					"All HIGH-severity issues must be triaged before sign-off (%d remaining)."
							.formatted(undecided));
		}
	}

	private static SignoffDecision parseDecision(String value) {
		try {
			return SignoffDecision.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException | NullPointerException ex) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_DECISION",
					"Decision must be APPROVED or REJECTED.");
		}
	}

	/** Non-repudiation stamp: SHA-256 of the typed signature bound to the signer and the moment. */
	private static String hash(String signature, UUID actorId) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String material = signature.trim() + "|" + actorId + "|" + Instant.now();
			return HexFormat.of().formatHex(digest.digest(material.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 unavailable", ex);
		}
	}

	private Map<UUID, UserBrief> resolveActors(java.util.stream.Stream<UUID> ids) {
		return identityFacade.findBriefs(ids.filter(Objects::nonNull).collect(Collectors.toSet()));
	}

	private static String nameOf(Map<UUID, UserBrief> actors, UUID actorId) {
		if (actorId == null) {
			return null;
		}
		UserBrief brief = actors.get(actorId);
		return brief == null ? null : brief.fullName();
	}

	private static String idString(UUID id) {
		return id == null ? null : id.toString();
	}
}
