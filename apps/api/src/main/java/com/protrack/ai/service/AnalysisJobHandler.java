package com.protrack.ai.service;

import com.protrack.ai.client.AiServiceClient;
import com.protrack.ai.client.AiServiceClient.AiAnalysisResult;
import com.protrack.ai.client.dto.AnalysisRequest;
import com.protrack.ai.domain.AiJob;
import com.protrack.ai.domain.JobType;
import com.protrack.ai.repository.AiJobRepository;
import com.protrack.analysis.service.AnalysisResultService;
import com.protrack.analysis.service.AnalysisResultService.Persisted;
import com.protrack.files.spi.FilesFacade;
import com.protrack.files.spi.FilesFacade.FileRef;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectContextInfo;
import com.protrack.shared.events.AiEvents;
import com.protrack.workflow.spi.WorkflowFacade;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * {@link AiJobHandler} for {@code MANUSCRIPT_ANALYSIS}: calls the FastAPI analyze endpoint, then
 * persists the normalized {@code analysis_*} rows, marks the job SUCCEEDED, advances the project
 * from INTAKE to AI_ANALYSIS (if that's still a valid move — see {@link WorkflowFacade}), and
 * publishes {@code AnalysisCompleted} — all inside the worker's second transaction.
 */
@Component
public class AnalysisJobHandler implements AiJobHandler {

	private static final Duration SIGNED_URL_TTL = Duration.ofMinutes(15);
	private static final String AI_ANALYSIS_STAGE = "AI_ANALYSIS";

	private final AiJobRepository aiJobRepository;
	private final AiServiceClient aiServiceClient;
	private final AnalysisResultService analysisResultService;
	private final FilesFacade filesFacade;
	private final ProjectFacade projectFacade;
	private final WorkflowFacade workflowFacade;
	private final ApplicationEventPublisher eventPublisher;

	public AnalysisJobHandler(AiJobRepository aiJobRepository, AiServiceClient aiServiceClient,
			AnalysisResultService analysisResultService, FilesFacade filesFacade,
			ProjectFacade projectFacade, WorkflowFacade workflowFacade,
			ApplicationEventPublisher eventPublisher) {
		this.aiJobRepository = aiJobRepository;
		this.aiServiceClient = aiServiceClient;
		this.analysisResultService = analysisResultService;
		this.filesFacade = filesFacade;
		this.projectFacade = projectFacade;
		this.workflowFacade = workflowFacade;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public JobType jobType() {
		return JobType.MANUSCRIPT_ANALYSIS;
	}

	@Override
	public Object callAiService(UUID jobId, AiJobContext context) {
		URI fileUrl = filesFacade.signedDownloadUrl(context.inputVersionId(), SIGNED_URL_TTL)
				.orElseThrow(() -> new IllegalStateException("No downloadable version for analysis."));
		FileRef version = filesFacade.resolveVersion(context.inputVersionId())
				.orElseThrow(() -> new IllegalStateException("Input version no longer exists."));
		ProjectContextInfo projectContext = projectFacade.findContext(context.projectId()).orElse(null);

		// The AI service selects a parser by file format, so send the version's mime type
		// (e.g. the DOCX/PDF content type), not the logical document type (MANUSCRIPT).
		AnalysisRequest request = new AnalysisRequest(
				jobId.toString(), fileUrl.toString(), version.mimeType(),
				new AnalysisRequest.ProjectContextDto(
						context.projectId().toString(),
						projectContext == null ? null : projectContext.title(),
						projectContext == null ? null : projectContext.publicationType(),
						projectContext == null ? null : projectContext.discipline()));
		return aiServiceClient.analyzeManuscript(request);
	}

	@Override
	public void persistResult(UUID jobId, AiJobContext context, Object result) {
		AiAnalysisResult analysis = (AiAnalysisResult) result;
		String model = analysis.response().model();
		UUID organizationId = projectFacade.findContext(context.projectId())
				.map(ProjectContextInfo::organizationId).orElse(null);

		Persisted persisted = analysisResultService.persist(
				context.projectId(), jobId, analysis.response(), analysis.rawPayload());
		AiJob job = aiJobRepository.findById(jobId).orElseThrow();
		job.markSucceeded(providerOf(model), model);
		aiJobRepository.save(job);
		workflowFacade.advanceIfValid(context.projectId(), context.createdBy(), AI_ANALYSIS_STAGE,
				"AI analysis completed.");
		eventPublisher.publishEvent(new AiEvents.AnalysisCompleted(
				organizationId, context.projectId(), context.createdBy(), jobId,
				persisted.analysisResultId(), persisted.overallConfidence()));
	}

	// Package-private (not private) so the unit test can call it directly.
	static String providerOf(String model) {
		if (model == null) {
			return "unknown";
		}
		if (model.startsWith("claude")) {
			return "claude";
		}
		if (model.startsWith("gemini")) {
			return "gemini";
		}
		return model.equals("mock") ? "mock" : model;
	}
}
