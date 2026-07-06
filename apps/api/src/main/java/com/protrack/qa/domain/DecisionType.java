package com.protrack.qa.domain;

import com.protrack.preflight.domain.IssueStatus;

/** A QA triage decision on an issue (matches the {@code qa_issue_decisions.decision} CHECK). */
public enum DecisionType {

	/** QA accepts the recommendation; the issue is resolved. */
	ACCEPT_FIX(IssueStatus.RESOLVED),

	/** QA sends the PDF back for a fix; the issue stays triaged pending re-work. */
	SEND_BACK(IssueStatus.TRIAGED),

	/** QA leaves a clarifying comment; the issue is triaged. */
	COMMENT(IssueStatus.TRIAGED);

	private final IssueStatus resultingStatus;

	DecisionType(IssueStatus resultingStatus) {
		this.resultingStatus = resultingStatus;
	}

	/** The derived issue status a decision moves the issue to. */
	public IssueStatus resultingStatus() {
		return resultingStatus;
	}

	/** True when a free-text comment is required to justify the decision. */
	public boolean requiresComment() {
		return this != ACCEPT_FIX;
	}
}
