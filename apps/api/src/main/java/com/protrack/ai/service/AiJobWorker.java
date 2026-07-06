package com.protrack.ai.service;

import com.protrack.ai.domain.AiJob;
import com.protrack.ai.domain.JobStatus;
import com.protrack.ai.domain.JobType;
import com.protrack.ai.repository.AiJobRepository;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectContextInfo;
import com.protrack.shared.events.AiEvents;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs an AI job off the request path, generically over its {@link JobType}: a short tx marks
 * RUNNING, then the external FastAPI call happens outside any transaction, then a short tx persists
 * the normalized result and marks SUCCEEDED — or marks FAILED on any error (never a 500 to the
 * user). The two type-specific steps are delegated to an {@link AiJobHandler}. Progress reaches
 * clients via FastAPI callbacks (SSE); this worker also emits terminal SSE events.
 */
@Component
public class AiJobWorker {

	private static final Logger log = LoggerFactory.getLogger(AiJobWorker.class);

	private final AiJobRepository aiJobRepository;
	private final ProjectFacade projectFacade;
	private final SseService sseService;
	private final ApplicationEventPublisher eventPublisher;
	private final TransactionTemplate txTemplate;
	private final Map<JobType, AiJobHandler> handlers = new EnumMap<>(JobType.class);

	public AiJobWorker(AiJobRepository aiJobRepository, ProjectFacade projectFacade,
			SseService sseService, ApplicationEventPublisher eventPublisher,
			PlatformTransactionManager txManager, List<AiJobHandler> handlerBeans) {
		this.aiJobRepository = aiJobRepository;
		this.projectFacade = projectFacade;
		this.sseService = sseService;
		this.eventPublisher = eventPublisher;
		this.txTemplate = new TransactionTemplate(txManager);
		for (AiJobHandler handler : handlerBeans) {
			this.handlers.put(handler.jobType(), handler);
		}
	}

	public void process(UUID jobId) {
		AiJobContext context = txTemplate.execute(status -> {
			AiJob job = aiJobRepository.findById(jobId).orElseThrow();
			job.markRunning();
			aiJobRepository.save(job);
			return new AiJobContext(job.getProjectId(), job.getInputVersionId(), job.getCreatedBy(),
					JobType.valueOf(job.getJobType()));
		});
		if (context == null) {
			return;
		}

		AiJobHandler handler = handlers.get(context.jobType());
		if (handler == null) {
			failJob(jobId, context, new IllegalStateException(
					"No handler for job type " + context.jobType()));
			return;
		}

		try {
			Object result = handler.callAiService(jobId, context);
			txTemplate.executeWithoutResult(status -> handler.persistResult(jobId, context, result));
			sseService.publish(context.projectId(), "completed", Map.of(
					"jobId", jobId.toString(), "status", JobStatus.SUCCEEDED.name(), "progressPct", 100));
		} catch (Exception ex) {
			failJob(jobId, context, ex);
		}
	}

	private void failJob(UUID jobId, AiJobContext context, Exception ex) {
		String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
		log.error("AI job {} ({}) failed: {}", jobId, context.jobType(), message, ex);
		UUID organizationId = projectFacade.findContext(context.projectId())
				.map(ProjectContextInfo::organizationId).orElse(null);

		txTemplate.executeWithoutResult(status -> {
			AiJob job = aiJobRepository.findById(jobId).orElseThrow();
			job.markFailed(message);
			aiJobRepository.save(job);
			eventPublisher.publishEvent(new AiEvents.AiJobFailed(
					organizationId, context.projectId(), context.createdBy(), jobId,
					context.jobType().name(), message));
		});

		sseService.publish(context.projectId(), "failed", Map.of(
				"jobId", jobId.toString(), "status", JobStatus.FAILED.name(), "error", message));
	}
}
