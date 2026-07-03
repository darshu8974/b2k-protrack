package com.protrack.analysis.service;

import com.protrack.ai.client.dto.AnalysisResponse;
import com.protrack.analysis.domain.AnalysisResult;
import com.protrack.analysis.mapper.AnalysisMapper;
import com.protrack.analysis.repository.AnalysisResultRepository;
import com.protrack.analysis.web.dto.AnalysisDetailResponse;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists normalized analysis results (1:1 with the AI response) and serves the latest result per
 * project. Consumed by the ai module's worker (write) and analysis read endpoint.
 */
@Service
public class AnalysisResultService {

	private final AnalysisResultRepository repository;
	private final AnalysisMapper mapper;

	public AnalysisResultService(AnalysisResultRepository repository, AnalysisMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	/** Minimal handle returned to the caller after persisting (no entity leakage across modules). */
	public record Persisted(UUID analysisResultId, Integer overallConfidence) {
	}

	@Transactional
	public Persisted persist(UUID projectId, UUID aiJobId, AnalysisResponse response,
			String rawPayload) {
		AnalysisResult saved = repository.save(mapper.toEntity(aiJobId, projectId, response, rawPayload));
		return new Persisted(saved.getId(), toInt(saved.getOverallConfidence()));
	}

	@Transactional(readOnly = true)
	public Optional<AnalysisDetailResponse> getLatest(UUID projectId) {
		return repository.findFirstByProjectIdOrderByCreatedAtDesc(projectId).map(this::toDetail);
	}

	private AnalysisDetailResponse toDetail(AnalysisResult r) {
		return new AnalysisDetailResponse(
				r.getId().toString(),
				r.getProjectId().toString(),
				r.getAiJobId().toString(),
				toInt(r.getOverallConfidence()),
				r.getSummary(),
				r.getLanguage(),
				r.getComplexityScore(),
				r.getComplexityLabel(),
				r.getEstimatedWorkingDays(),
				r.getMetrics().stream().map(m -> new AnalysisDetailResponse.MetricView(
						m.getMetricKey(), m.getMetricValue(), toInt(m.getConfidence()))).toList(),
				r.getComposition().stream().map(c -> new AnalysisDetailResponse.CompositionView(
						c.getSegment(), toDouble(c.getPercentage()))).toList(),
				r.getHeadings().stream().map(h -> new AnalysisDetailResponse.HeadingView(
						h.getLevel(), h.getCount())).toList(),
				r.getRisks().stream().map(risk -> new AnalysisDetailResponse.RiskView(
						risk.getSeverity(), risk.getTitle(), risk.getDescription())).toList(),
				r.getTeamSuggestions().stream().map(t -> new AnalysisDetailResponse.TeamView(
						t.getSuggestedUserId() == null ? null : t.getSuggestedUserId().toString(),
						t.getSuggestedRole(), toInt(t.getMatchScore()), t.getRationale())).toList(),
				r.getCreatedAt());
	}

	private static Integer toInt(BigDecimal value) {
		return value == null ? null : value.intValue();
	}

	private static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}
}
