package com.protrack.workflow.domain;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * The allowed stage transitions (the state machine). Each rule names the source/target stage, the
 * roles permitted to perform it, and whether it is an explicit approval gate. ADMIN may perform any
 * transition (handled in the service as an override).
 */
public enum StageTransition {

	START_ANALYSIS("INTAKE", "AI_ANALYSIS", false, "PM"),
	APPROVE_ANALYSIS("AI_ANALYSIS", "DESIGN_PREP", true, "PM"),
	HAND_OFF("DESIGN_PREP", "IN_PRODUCTION", false, "PM"),
	SUBMIT_PDF("IN_PRODUCTION", "PDF_REVIEW", false, "DESIGNER"),
	SEND_TO_QA("PDF_REVIEW", "QA_SIGNOFF", false, "QA", "PM"),
	SIGN_OFF("QA_SIGNOFF", "COMPLETED", true, "QA"),
	REJECT_FROM_PDF_REVIEW("PDF_REVIEW", "IN_PRODUCTION", false, "QA", "PM"),
	REJECT_FROM_QA("QA_SIGNOFF", "IN_PRODUCTION", false, "QA");

	private final String from;
	private final String to;
	private final boolean approvalGate;
	private final Set<String> roles;

	StageTransition(String from, String to, boolean approvalGate, String... roles) {
		this.from = from;
		this.to = to;
		this.approvalGate = approvalGate;
		this.roles = Set.of(roles);
	}

	public String from() {
		return from;
	}

	public String to() {
		return to;
	}

	public boolean approvalGate() {
		return approvalGate;
	}

	public Set<String> roles() {
		return roles;
	}

	/** Find the rule matching a (from, to) pair, if the transition is allowed. */
	public static Optional<StageTransition> find(String from, String to) {
		return Arrays.stream(values())
				.filter(rule -> rule.from.equals(from) && rule.to.equals(to))
				.findFirst();
	}
}
