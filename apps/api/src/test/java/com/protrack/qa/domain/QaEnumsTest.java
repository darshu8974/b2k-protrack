package com.protrack.qa.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.protrack.preflight.domain.IssueStatus;
import org.junit.jupiter.api.Test;

/** Unit tests for the QA decision/sign-off enums (no Spring context, no Docker). */
class QaEnumsTest {

	@Test
	void decisionResultingStatusMapping() {
		assertThat(DecisionType.ACCEPT_FIX.resultingStatus()).isEqualTo(IssueStatus.RESOLVED);
		assertThat(DecisionType.SEND_BACK.resultingStatus()).isEqualTo(IssueStatus.TRIAGED);
		assertThat(DecisionType.COMMENT.resultingStatus()).isEqualTo(IssueStatus.TRIAGED);
	}

	@Test
	void onlyAcceptFixSkipsTheCommentRequirement() {
		assertThat(DecisionType.ACCEPT_FIX.requiresComment()).isFalse();
		assertThat(DecisionType.SEND_BACK.requiresComment()).isTrue();
		assertThat(DecisionType.COMMENT.requiresComment()).isTrue();
	}

	@Test
	void signoffDecisionTargetStages() {
		assertThat(SignoffDecision.APPROVED.targetStage()).isEqualTo("COMPLETED");
		assertThat(SignoffDecision.REJECTED.targetStage()).isEqualTo("IN_PRODUCTION");
	}
}
