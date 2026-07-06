package com.protrack.preflight.service;

import com.protrack.ai.client.dto.PreflightResponse;
import com.protrack.preflight.domain.PreflightCheck;
import com.protrack.preflight.domain.PreflightRun;
import com.protrack.preflight.domain.QaIssue;
import com.protrack.preflight.mapper.PreflightMapper;
import com.protrack.preflight.mapper.PreflightMapper.Mapped;
import com.protrack.preflight.repository.PreflightRunRepository;
import com.protrack.preflight.repository.QaIssueRepository;
import com.protrack.preflight.web.dto.PreflightDetailResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists normalized preflight results (run + checks + raised issues, 1:1 with the AI response) and
 * serves the latest run per project. Consumed by the ai module's worker (write) and the preflight
 * read endpoint. The persist runs inside the worker's result transaction, so run/checks/issues and
 * the job's SUCCEEDED state commit atomically.
 */
@Service
public class PreflightResultService {

	private final PreflightRunRepository runRepository;
	private final QaIssueRepository issueRepository;
	private final PreflightMapper mapper;

	public PreflightResultService(PreflightRunRepository runRepository,
			QaIssueRepository issueRepository, PreflightMapper mapper) {
		this.runRepository = runRepository;
		this.issueRepository = issueRepository;
		this.mapper = mapper;
	}

	/** Minimal handle returned after persisting (no entity leakage across modules). */
	public record Persisted(UUID preflightRunId, Integer overallScore, Boolean passed,
			int totalIssues, int highSeverity) {
	}

	@Transactional
	public Persisted persist(UUID projectId, UUID aiJobId, UUID pdfVersionId,
			PreflightResponse response) {
		Mapped mapped = mapper.toEntities(aiJobId, projectId, pdfVersionId, response);
		PreflightRun run = runRepository.save(mapped.run());
		if (!mapped.issues().isEmpty()) {
			issueRepository.saveAll(mapped.issues());
		}
		return new Persisted(run.getId(), run.getOverallScore(), run.getPassed(),
				run.getTotalIssues(), run.getHighSeverity());
	}

	@Transactional(readOnly = true)
	public Optional<PreflightDetailResponse> getLatest(UUID projectId) {
		return runRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
				.map(this::toDetail);
	}

	private PreflightDetailResponse toDetail(PreflightRun run) {
		List<PreflightDetailResponse.CheckView> checks = run.getChecks().stream()
				.sorted(Comparator.comparingInt(PreflightCheck::getSortOrder))
				.map(check -> new PreflightDetailResponse.CheckView(
						check.getCheckKey(), check.getResult(), check.getDetail()))
				.toList();
		List<PreflightDetailResponse.IssueView> issues =
				issueRepository.findByPreflightRunIdOrderByCreatedAtDesc(run.getId()).stream()
						.map(PreflightResultService::toIssueView).toList();
		return new PreflightDetailResponse(
				run.getId().toString(), run.getProjectId().toString(), run.getAiJobId().toString(),
				run.getPdfVersionId().toString(), run.getStandard(), run.getOverallScore(),
				run.getPassed(), run.getTotalIssues(), run.getHighSeverity(), run.getStatus(),
				run.getRanAt(), run.getCreatedAt(), checks, issues);
	}

	private static PreflightDetailResponse.IssueView toIssueView(QaIssue issue) {
		return new PreflightDetailResponse.IssueView(
				issue.getId().toString(), issue.getCategory(), issue.getSeverity(), issue.getTitle(),
				issue.getRecommendation(), issue.getPageRef(), issue.getSource(), issue.getStatus(),
				issue.getCreatedAt());
	}
}
