package com.protrack.ai.domain;

/** AI job lifecycle status (matches the {@code ai_jobs.status} CHECK). */
public enum JobStatus {
	QUEUED,
	RUNNING,
	SUCCEEDED,
	FAILED
}
