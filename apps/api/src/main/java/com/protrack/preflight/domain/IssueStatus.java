package com.protrack.preflight.domain;

/** Triage status of a QA issue (matches the {@code qa_issues.status} CHECK). */
public enum IssueStatus {
	OPEN,
	TRIAGED,
	RESOLVED,
	WAIVED
}
