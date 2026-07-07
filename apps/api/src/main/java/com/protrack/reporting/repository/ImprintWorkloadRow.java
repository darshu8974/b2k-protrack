package com.protrack.reporting.repository;

/** Active-project count for one imprint (projection for the workload native query). */
public interface ImprintWorkloadRow {

	/** Imprint id as text, or {@code UNASSIGNED} for projects with no imprint. */
	String getImprintId();

	String getImprintName();

	long getActiveProjects();
}
