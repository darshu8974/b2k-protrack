package com.protrack.ai.client.dto;

import java.util.List;

/**
 * The normalized manuscript-analysis result returned by the FastAPI AI service (camelCase JSON,
 * deserialized 1:1). The analysis module maps this into the {@code analysis_*} tables.
 */
public record AnalysisResponse(
		Integer overallConfidence,
		String summary,
		String language,
		Integer complexityScore,
		String complexityLabel,
		Integer estimatedWorkingDays,
		List<MetricDto> metrics,
		List<CompositionDto> composition,
		List<HeadingDto> headings,
		List<RiskDto> risks,
		List<TeamDto> suggestedTeam,
		String promptId,
		String model,
		UsageDto usage) {

	public record MetricDto(String key, Long value, Integer confidence) {
	}

	public record CompositionDto(String segment, Double percentage) {
	}

	public record HeadingDto(String level, Integer count) {
	}

	public record RiskDto(String severity, String title, String description) {
	}

	public record TeamDto(String role, Integer matchScore, String rationale, String candidateHint) {
	}

	public record UsageDto(Integer inputTokens, Integer outputTokens, String model) {
	}
}
