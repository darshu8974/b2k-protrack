package com.protrack.analysis.web.dto;

import java.time.Instant;
import java.util.List;

/** Persisted manuscript analysis returned to the client (camelCase). */
public record AnalysisDetailResponse(
		String id,
		String projectId,
		String aiJobId,
		Integer overallConfidence,
		String summary,
		String language,
		Integer complexityScore,
		String complexityLabel,
		Integer estimatedWorkingDays,
		List<MetricView> metrics,
		List<CompositionView> composition,
		List<HeadingView> headings,
		List<RiskView> risks,
		List<TeamView> suggestedTeam,
		Instant createdAt) {

	public record MetricView(String key, Long value, Integer confidence) {
	}

	public record CompositionView(String segment, Double percentage) {
	}

	public record HeadingView(String level, int count) {
	}

	public record RiskView(String severity, String title, String description) {
	}

	public record TeamView(String userId, String role, Integer matchScore, String rationale) {
	}
}
