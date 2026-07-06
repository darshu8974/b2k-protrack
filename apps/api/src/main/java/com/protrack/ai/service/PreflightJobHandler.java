package com.protrack.ai.service;

import com.protrack.ai.client.AiServiceClient;
import com.protrack.ai.client.AiServiceClient.AiPreflightResult;
import com.protrack.ai.client.dto.PreflightRequest;
import com.protrack.ai.domain.AiJob;
import com.protrack.ai.domain.JobType;
import com.protrack.ai.repository.AiJobRepository;
import com.protrack.files.spi.FilesFacade;
import com.protrack.preflight.service.PreflightResultService;
import com.protrack.preflight.service.PreflightResultService.Persisted;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectContextInfo;
import com.protrack.shared.events.AiEvents;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * {@link AiJobHandler} for {@code PDF_PREFLIGHT}: calls the FastAPI preflight endpoint with a signed
 * URL to the production PDF, then persists the {@code preflight_runs}/{@code preflight_checks}/
 * {@code qa_issues} rows, marks the job SUCCEEDED, and publishes {@code PreflightCompleted} — all
 * inside the worker's second transaction.
 */
@Component
public class PreflightJobHandler implements AiJobHandler {

	private static final Duration SIGNED_URL_TTL = Duration.ofMinutes(15);

	private final AiJobRepository aiJobRepository;
	private final AiServiceClient aiServiceClient;
	private final PreflightResultService preflightResultService;
	private final FilesFacade filesFacade;
	private final ProjectFacade projectFacade;
	private final ApplicationEventPublisher eventPublisher;

	public PreflightJobHandler(AiJobRepository aiJobRepository, AiServiceClient aiServiceClient,
			PreflightResultService preflightResultService, FilesFacade filesFacade,
			ProjectFacade projectFacade, ApplicationEventPublisher eventPublisher) {
		this.aiJobRepository = aiJobRepository;
		this.aiServiceClient = aiServiceClient;
		this.preflightResultService = preflightResultService;
		this.filesFacade = filesFacade;
		this.projectFacade = projectFacade;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public JobType jobType() {
		return JobType.PDF_PREFLIGHT;
	}

	@Override
	public Object callAiService(UUID jobId, AiJobContext context) {
		URI fileUrl = filesFacade.signedDownloadUrl(context.inputVersionId(), SIGNED_URL_TTL)
				.orElseThrow(() -> new IllegalStateException("No downloadable PDF version for preflight."));
		// Standard is not selected in Phase 1; the AI service treats null as "unspecified".
		PreflightRequest request = new PreflightRequest(jobId.toString(), fileUrl.toString(), null);
		return aiServiceClient.preflightPdf(request);
	}

	@Override
	public void persistResult(UUID jobId, AiJobContext context, Object result) {
		AiPreflightResult preflight = (AiPreflightResult) result;
		String model = preflight.response().model();
		UUID organizationId = projectFacade.findContext(context.projectId())
				.map(ProjectContextInfo::organizationId).orElse(null);

		Persisted persisted = preflightResultService.persist(
				context.projectId(), jobId, context.inputVersionId(), preflight.response());
		AiJob job = aiJobRepository.findById(jobId).orElseThrow();
		job.markSucceeded(providerOf(model), model);
		aiJobRepository.save(job);
		eventPublisher.publishEvent(new AiEvents.PreflightCompleted(
				organizationId, context.projectId(), context.createdBy(), jobId,
				persisted.preflightRunId(), persisted.overallScore(), persisted.passed(),
				persisted.totalIssues(), persisted.highSeverity()));
	}

	private static String providerOf(String model) {
		if (model == null) {
			return "unknown";
		}
		return model.startsWith("claude") ? "claude" : model;
	}
}
