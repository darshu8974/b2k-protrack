package com.protrack.project.web.dto;

import java.util.List;

/** Aggregated dashboard payload for the current user's organization. */
public record DashboardResponse(
		Kpis kpis,
		List<StageCount> stageCounts,
		List<StatusCount> statusCounts,
		List<ProjectSummaryResponse> recentProjects,
		List<ProjectSummaryResponse> myProjects) {

	/** Headline KPI numbers. */
	public record Kpis(
			long activeProjects,
			long inProduction,
			long awaitingQa,
			long completedThisMonth,
			long totalProjects) {
	}

	/** Count of projects currently at a pipeline stage. */
	public record StageCount(String stage, long count) {
	}

	/** Count of projects in a lifecycle status. */
	public record StatusCount(String status, long count) {
	}
}
