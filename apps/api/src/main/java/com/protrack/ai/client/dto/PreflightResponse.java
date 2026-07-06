package com.protrack.ai.client.dto;

import java.util.List;

/**
 * The normalized PDF-preflight result returned by the FastAPI AI service (camelCase JSON,
 * deserialized 1:1). The preflight module maps this into {@code preflight_runs} /
 * {@code preflight_checks} / {@code qa_issues}.
 */
public record PreflightResponse(
		Integer overallScore,
		Boolean passed,
		String standard,
		List<CheckDto> checks,
		List<IssueDto> issues,
		TotalsDto totals,
		String promptId,
		String model,
		UsageDto usage) {

	public record CheckDto(String key, String result, String detail) {
	}

	public record IssueDto(String category, String severity, String title, String recommendation,
			String pageRef, String source) {
	}

	public record TotalsDto(Integer issues, Integer high) {
	}

	public record UsageDto(Integer inputTokens, Integer outputTokens, String model) {
	}
}
