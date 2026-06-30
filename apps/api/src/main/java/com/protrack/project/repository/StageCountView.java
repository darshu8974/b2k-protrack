package com.protrack.project.repository;

/** Projection for a grouped count of projects by current stage. */
public interface StageCountView {
	String getStage();

	long getTotal();
}
