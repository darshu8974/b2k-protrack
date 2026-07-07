package com.protrack.reporting.web.dto;

import java.time.LocalDate;

/**
 * Headline KPIs for the Reports screen (API Specification §3.15). Numeric KPIs are nullable — a
 * {@code null} means "no data in the window" so the UI can show "—" rather than a misleading 0.
 * Percentages are 0–100; {@code turnaroundDays} is an average day count.
 */
public record ReportOverviewResponse(
		String range,
		LocalDate periodStart,
		LocalDate periodEnd,
		Double turnaroundDays,
		Double onTimePercentage,
		Double avgAiConfidence,
		Double qaPassPercentage,
		long completedProjects,
		long qaSignoffs) {
}
