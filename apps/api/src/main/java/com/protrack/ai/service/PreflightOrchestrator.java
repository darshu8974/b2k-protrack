package com.protrack.ai.service;

import com.protrack.ai.domain.AiJob;
import com.protrack.ai.domain.JobType;
import com.protrack.ai.repository.AiJobRepository;
import com.protrack.ai.web.dto.AiJobResponse;
import com.protrack.files.spi.FilesFacade;
import com.protrack.files.spi.FilesFacade.FileRef;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectContextInfo;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.events.AiEvents;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Starts a PDF preflight: a short transaction creates the {@code AiJob(QUEUED)} against the project's
 * current production PDF and returns 202; the FastAPI call is dispatched asynchronously only after
 * that transaction commits. Mirrors {@link AnalysisOrchestrator}, reusing the same async pipeline.
 */
@Service
public class PreflightOrchestrator {

	private static final String PRODUCTION_PDF_DOC_TYPE = "PRODUCTION_PDF";

	private final AiJobRepository aiJobRepository;
	private final FilesFacade filesFacade;
	private final ProjectFacade projectFacade;
	private final AiJobDispatcher dispatcher;
	private final ApplicationEventPublisher eventPublisher;

	public PreflightOrchestrator(AiJobRepository aiJobRepository, FilesFacade filesFacade,
			ProjectFacade projectFacade, AiJobDispatcher dispatcher,
			ApplicationEventPublisher eventPublisher) {
		this.aiJobRepository = aiJobRepository;
		this.filesFacade = filesFacade;
		this.projectFacade = projectFacade;
		this.dispatcher = dispatcher;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public AiJobResponse startPreflight(UUID actorId, UUID projectId) {
		ProjectContextInfo context = projectFacade.findContext(projectId)
				.orElseThrow(() -> new NotFoundException("Project not found."));

		FileRef pdf = filesFacade.listCurrentFilesForProject(projectId).stream()
				.filter(file -> PRODUCTION_PDF_DOC_TYPE.equals(file.docType()))
				.findFirst()
				.orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "NO_PRODUCTION_PDF",
						"Upload a production PDF before running preflight."));

		AiJob saved = aiJobRepository.save(new AiJob(
				UUID.randomUUID(), projectId, JobType.PDF_PREFLIGHT, pdf.versionId(), actorId));

		eventPublisher.publishEvent(new AiEvents.AiJobStarted(
				context.organizationId(), projectId, actorId, saved.getId(),
				JobType.PDF_PREFLIGHT.name()));

		// Dispatch only after commit so the async worker sees the persisted job.
		UUID jobId = saved.getId();
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				dispatcher.dispatch(jobId);
			}
		});

		return AiJobResponse.from(saved);
	}
}
