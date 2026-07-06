package com.protrack.preflight.spi;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Published interface of the preflight module. Lets the qa module read the issues a run raised and
 * update their derived triage status, and resolve the latest run to certify at sign-off — without
 * depending on the preflight entities directly (preserves module boundaries).
 */
public interface PreflightFacade {

	/** A resolved QA issue projection (no entity leakage). */
	record IssueRef(UUID id, UUID projectId, UUID preflightRunId, String category, String severity,
			String title, String recommendation, String pageRef, String source, String status,
			Instant createdAt) {
	}

	/** The latest preflight run for a project (the artifact a sign-off certifies). */
	record RunRef(UUID id, UUID projectId, Integer overallScore, Boolean passed) {
	}

	/** Issues for a project, optionally filtered by severity and/or status (null = no filter). */
	List<IssueRef> findIssues(UUID projectId, String severity, String status);

	Optional<IssueRef> findIssue(UUID issueId);

	/** All issues of a given severity for a project (used by the sign-off gate). */
	List<IssueRef> findIssuesBySeverity(UUID projectId, String severity);

	/** Update an issue's derived triage status (the decision history remains the source of truth). */
	void applyIssueStatus(UUID issueId, String status);

	Optional<RunRef> findLatestRun(UUID projectId);
}
