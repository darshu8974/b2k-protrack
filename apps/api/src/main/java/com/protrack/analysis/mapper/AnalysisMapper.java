package com.protrack.analysis.mapper;

import com.protrack.ai.client.dto.AnalysisResponse;
import com.protrack.analysis.domain.AnalysisComposition;
import com.protrack.analysis.domain.AnalysisHeading;
import com.protrack.analysis.domain.AnalysisMetric;
import com.protrack.analysis.domain.AnalysisResult;
import com.protrack.analysis.domain.AnalysisRisk;
import com.protrack.analysis.domain.TeamSuggestion;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Maps the AI service's normalized {@link AnalysisResponse} into the {@code analysis_*} entity graph.
 * The mapping is mechanical (the response already conforms to the DB conventions); {@code rawPayload}
 * preserves the full response for provenance. Team suggestions keep the role/score/rationale; the
 * user resolution is left for a later human-triggered step.
 */
@Component
public class AnalysisMapper {

	public AnalysisResult toEntity(UUID aiJobId, UUID projectId, AnalysisResponse response,
			String rawPayload) {
		AnalysisResult result = new AnalysisResult(
				UUID.randomUUID(), aiJobId, projectId,
				toDecimal(response.overallConfidence()), response.summary(), response.language(),
				response.complexityScore(), response.complexityLabel(),
				response.estimatedWorkingDays(), rawPayload);

		for (AnalysisResponse.MetricDto metric : nullSafe(response.metrics())) {
			result.getMetrics().add(new AnalysisMetric(
					UUID.randomUUID(), result, metric.key(), metric.value(),
					toDecimal(metric.confidence())));
		}
		for (AnalysisResponse.CompositionDto segment : nullSafe(response.composition())) {
			result.getComposition().add(new AnalysisComposition(
					UUID.randomUUID(), result, segment.segment(), toDecimal(segment.percentage())));
		}
		for (AnalysisResponse.HeadingDto heading : nullSafe(response.headings())) {
			result.getHeadings().add(new AnalysisHeading(
					UUID.randomUUID(), result, heading.level(),
					heading.count() == null ? 0 : heading.count()));
		}
		for (AnalysisResponse.RiskDto risk : nullSafe(response.risks())) {
			result.getRisks().add(new AnalysisRisk(
					UUID.randomUUID(), result, risk.severity(), risk.title(), risk.description()));
		}
		for (AnalysisResponse.TeamDto member : nullSafe(response.suggestedTeam())) {
			result.getTeamSuggestions().add(new TeamSuggestion(
					UUID.randomUUID(), result, null, member.role(),
					toDecimal(member.matchScore()), member.rationale()));
		}
		return result;
	}

	private static <T> List<T> nullSafe(List<T> list) {
		return list == null ? List.of() : list;
	}

	private static BigDecimal toDecimal(Integer value) {
		return value == null ? null : BigDecimal.valueOf(value);
	}

	private static BigDecimal toDecimal(Double value) {
		return value == null ? null : BigDecimal.valueOf(value);
	}
}
