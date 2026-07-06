package com.protrack.qa.domain;

/** The outcome of a QA sign-off (matches the {@code qa_signoffs.decision} CHECK). */
public enum SignoffDecision {

	/** Approved: the project completes. */
	APPROVED("COMPLETED"),

	/** Rejected: the project is sent back to production. */
	REJECTED("IN_PRODUCTION");

	private final String targetStage;

	SignoffDecision(String targetStage) {
		this.targetStage = targetStage;
	}

	/** The workflow stage the project transitions to on this decision. */
	public String targetStage() {
		return targetStage;
	}
}
