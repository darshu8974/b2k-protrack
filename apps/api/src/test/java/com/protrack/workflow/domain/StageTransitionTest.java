package com.protrack.workflow.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for the {@link StageTransition} state machine (no Spring context, no Docker). */
class StageTransitionTest {

	@Test
	void allowsLegalForwardTransition() {
		var transition = StageTransition.find("INTAKE", "AI_ANALYSIS");
		assertThat(transition).isPresent();
		assertThat(transition.get().roles()).contains("PM");
		assertThat(transition.get().approvalGate()).isFalse();
	}

	@Test
	void signOffIsAnApprovalGateForQa() {
		var transition = StageTransition.find("QA_SIGNOFF", "COMPLETED");
		assertThat(transition).isPresent();
		assertThat(transition.get().approvalGate()).isTrue();
		assertThat(transition.get().roles()).contains("QA");
	}

	@Test
	void rejectsSkippingStages() {
		assertThat(StageTransition.find("INTAKE", "IN_PRODUCTION")).isEmpty();
	}

	@Test
	void completedIsTerminal() {
		assertThat(StageTransition.find("COMPLETED", "AI_ANALYSIS")).isEmpty();
	}
}
