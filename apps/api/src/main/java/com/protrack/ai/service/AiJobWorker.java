package com.protrack.ai.service;

import com.protrack.ai.client.AiServiceClient;
import com.protrack.ai.client.AiServiceClient.AiAnalysisResult;
import com.protrack.ai.client.dto.AnalysisRequest;
import com.protrack.ai.domain.AiJob;
import com.protrack.ai.domain.JobStatus;
import com.protrack.ai.domain.JobType;
import com.protrack.ai.repository.AiJobRepository;
import com.protrack.analysis.service.AnalysisResultService;
import com.protrack.analysis.service.AnalysisResultService.Persisted;
import com.protrack.files.spi.FilesFacade;
import com.protrack.files.spi.FilesFacade.FileRef;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectContextInfo;
import com.protrack.shared.events.AiEvents;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs a manuscript-analysis job off the request path: short tx to mark RUNNING, then the external
 * FastAPI call (outside any transaction), then a short tx to persist the normalized results and mark
 * SUCCEEDED — or mark FAILED on any error (never a 500 to the user). Progress reaches clients via
 * FastAPI callbacks (SSE); this worker also emits terminal SSE events.
 */
@Component
public class AiJobWorker {

	private static final Logger log = LoggerFactory.getLogger(AiJobWorker.class);
	private static final Duration SIGNED_URL_TTL = Duration.ofMinutes(15);

	private final AiJobRepository aiJobRepository;
	private final AiServiceClient aiServiceClient;
	private final AnalysisResultService analysisResultService;
	private final FilesFacade filesFacade;
	private final ProjectFacade projectFacade;
	private final SseService sseService;
	private final ApplicationEventPublisher eventPublisher;
	private final TransactionTemplate txTemplate;

	public AiJobWorker(AiJobRepository aiJobRepository, AiServiceClient aiServiceClient,
			AnalysisResultService analysisResultService, FilesFacade filesFacade,
			ProjectFacade projectFacade, SseService sseService,
			ApplicationEventPublisher eventPublisher, PlatformTransactionManager txManager) {
		this.aiJobRepository = aiJobRepository;
		this.aiServiceClient = aiServiceClient;
		this.analysisResultService = analysisResultService;
		this.filesFacade = filesFacade;
		this.projectFacade = projectFacade;
		this.sseService = sseService;
		this.eventPublisher = eventPublisher;
		this.txTemplate = new TransactionTemplate(txManager);
	}

	private record JobSnapshot(UUID projectId, UUID inputVersionId, UUID createdBy) {
	}

	public void process(UUID jobId) {
		JobSnapshot snapshot = txTemplate.execute(status -> {
			AiJob job = aiJobRepository.findById(jobId).orElseThrow();
			job.markRunning();
			aiJobRepository.save(job);
			return new JobSnapshot(job.getProjectId(), job.getInputVersionId(), job.getCreatedBy());
		});
		if (snapshot == null) {
			return;
		}

		try {
			AiAnalysisResult result = callAiService(jobId, snapshot);
			completeSuccessfully(jobId, snapshot, result);
		} catch (Exception ex) {
			failJob(jobId, snapshot, ex);
		}
	}

	private AiAnalysisResult callAiService(UUID jobId, JobSnapshot snapshot) {
		URI fileUrl = filesFacade.signedDownloadUrl(snapshot.inputVersionId(), SIGNED_URL_TTL)
				.orElseThrow(() -> new IllegalStateException("No downloadable version for analysis."));
		FileRef version = filesFacade.resolveVersion(snapshot.inputVersionId())
				.orElseThrow(() -> new IllegalStateException("Input version no longer exists."));
		ProjectContextInfo context = projectFacade.findContext(snapshot.projectId()).orElse(null);

		// The AI service selects a parser by file format, so send the version's mime type
		// (e.g. the DOCX/PDF content type), not the logical document type (MANUSCRIPT).
		AnalysisRequest request = new AnalysisRequest(
				jobId.toString(), fileUrl.toString(), version.mimeType(),
				new AnalysisRequest.ProjectContextDto(
						snapshot.projectId().toString(),
						context == null ? null : context.title(),
						context == null ? null : context.publicationType(),
						context == null ? null : context.discipline()));
		return aiServiceClient.analyzeManuscript(request);
	}

	private void completeSuccessfully(UUID jobId, JobSnapshot snapshot, AiAnalysisResult result) {
		String model = result.response().model();
		String provider = providerOf(model);
		UUID organizationId = projectFacade.findContext(snapshot.projectId())
				.map(ProjectContextInfo::organizationId).orElse(null);

		txTemplate.executeWithoutResult(status -> {
			Persisted persisted = analysisResultService.persist(
					snapshot.projectId(), jobId, result.response(), result.rawPayload());
			AiJob job = aiJobRepository.findById(jobId).orElseThrow();
			job.markSucceeded(provider, model);
			aiJobRepository.save(job);
			eventPublisher.publishEvent(new AiEvents.AnalysisCompleted(
					organizationId, snapshot.projectId(), snapshot.createdBy(), jobId,
					persisted.analysisResultId(), persisted.overallConfidence()));
		});

		sseService.publish(snapshot.projectId(), "completed", Map.of(
				"jobId", jobId.toString(), "status", JobStatus.SUCCEEDED.name(), "progressPct", 100));
	}

	private void failJob(UUID jobId, JobSnapshot snapshot, Exception ex) {
		String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
		log.error("AI job {} failed: {}", jobId, message, ex);
		UUID organizationId = projectFacade.findContext(snapshot.projectId())
				.map(ProjectContextInfo::organizationId).orElse(null);

		txTemplate.executeWithoutResult(status -> {
			AiJob job = aiJobRepository.findById(jobId).orElseThrow();
			job.markFailed(message);
			aiJobRepository.save(job);
			eventPublisher.publishEvent(new AiEvents.AiJobFailed(
					organizationId, snapshot.projectId(), snapshot.createdBy(), jobId,
					JobType.MANUSCRIPT_ANALYSIS.name(), message));
		});

		sseService.publish(snapshot.projectId(), "failed", Map.of(
				"jobId", jobId.toString(), "status", JobStatus.FAILED.name(), "error", message));
	}

	private static String providerOf(String model) {
		if (model == null) {
			return "unknown";
		}
		return model.startsWith("claude") ? "claude" : model;
	}
}
