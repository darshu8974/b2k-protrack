package com.protrack.reporting.web.dto;

import java.util.List;

/** Share of active projects by imprint (API Specification §3.15). */
public record ImprintWorkloadResponse(long totalActive, List<Item> items) {

	/** One imprint's active-project count and its share of the total (0–100). */
	public record Item(String imprintId, String imprintName, long activeProjects, double percentage) {
	}
}
