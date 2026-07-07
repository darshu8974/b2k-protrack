package com.protrack.reporting.repository;

/** One month's completion count (projection for the throughput native query). */
public interface ThroughputRow {

	/** Month as {@code YYYY-MM}. */
	String getMonth();

	long getCompleted();
}
