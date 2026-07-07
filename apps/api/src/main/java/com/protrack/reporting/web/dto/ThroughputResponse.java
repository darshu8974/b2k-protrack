package com.protrack.reporting.web.dto;

import java.util.List;

/** Titles completed per month over the window (API Specification §3.15). */
public record ThroughputResponse(String range, List<Point> points) {

	/** One month's completion count; {@code month} is {@code YYYY-MM}. */
	public record Point(String month, long completed) {
	}
}
