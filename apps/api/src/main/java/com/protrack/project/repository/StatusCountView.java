package com.protrack.project.repository;

/** Projection for a grouped count of projects by status. */
public interface StatusCountView {
	String getStatus();

	long getTotal();
}
