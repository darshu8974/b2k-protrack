package com.protrack.ai.service;

import com.protrack.ai.domain.AiJob;
import com.protrack.ai.domain.JobStatus;
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
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Starts manuscript analysis: a short transaction creates the {@code AiJob(QUEUED)} and returns 202;
 * the actual FastAPI call is dispatched asynchronously only after that transaction commits (so the
 * worker always sees a persisted job, and nothing runs for a rolled-back create).
 */
@Service
public class AnalysisOrchestrator {

	private static final String MANUSCRIPT_DOC_TYPE = "MANUSCRIPT";
	private static final List<String> ACTIVE_STATUSES = List.of(JobStatus.QUEUED.name(), JobStatus.RUNNING.name());

	private final AiJobRepository aiJobRepository;
	private final FilesFacade filesFacade;
	private final ProjectFacade projectFacade;
	private final AiJobDispatcher dispatcher;
	private final ApplicationEventPublisher eventPublisher;

	public AnalysisOrchestrator(AiJobRepository aiJobRepository, FilesFacade filesFacade,
			ProjectFacade projectFacade, AiJobDispatcher dispatcher,
			ApplicationEventPublisher eventPublisher) {
		this.aiJobRepository = aiJobRepository;
		this.filesFacade = filesFacade;
		this.projectFacade = projectFacade;
		this.dispatcher = dispatcher;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public AiJobResponse startManuscriptAnalysis(UUID actorId, UUID projectId) {
		ProjectContextInfo context = projectFacade.findContext(projectId)
				.orElseThrow(() -> new NotFoundException("Project not found."));

		if (aiJobRepository.existsByProjectIdAndJobTypeAndStatusIn(
				projectId, JobType.MANUSCRIPT_ANALYSIS.name(), ACTIVE_STATUSES)) {
			throw new ApiException(HttpStatus.CONFLICT, "ANALYSIS_IN_PROGRESS",
					"An analysis is already running for this project.");
		}

		FileRef manuscript = filesFacade.listCurrentFilesForProject(projectId).stream()
				.filter(file -> MANUSCRIPT_DOC_TYPE.equals(file.docType()))
				.findFirst()
				.orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "NO_MANUSCRIPT",
						"Upload a manuscript before running analysis."));

		AiJob saved = aiJobRepository.save(new AiJob(
				UUID.randomUUID(), projectId, JobType.MANUSCRIPT_ANALYSIS,
				manuscript.versionId(), actorId));

		eventPublisher.publishEvent(new AiEvents.AiJobStarted(
				context.organizationId(), projectId, actorId, saved.getId(),
				JobType.MANUSCRIPT_ANALYSIS.name()));

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
