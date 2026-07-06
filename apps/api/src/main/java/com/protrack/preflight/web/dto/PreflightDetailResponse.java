package com.protrack.preflight.web.dto;

import java.time.Instant;
import java.util.List;

/** The latest preflight run for a project: header, ordered checks, and the issues it raised. */
public record PreflightDetailResponse(
		String id,
		String projectId,
		String aiJobId,
		String pdfVersionId,
		String standard,
		Integer overallScore,
		Boolean passed,
		int totalIssues,
		int highSeverity,
		String status,
		Instant ranAt,
		Instant createdAt,
		List<CheckView> checks,
		List<IssueView> issues) {

	public record CheckView(String key, String result, String detail) {
	}

	public record IssueView(
			String id,
			String category,
			String severity,
			String title,
			String recommendation,
			String pageRef,
			String source,
			String status,
			Instant createdAt) {
	}
}
