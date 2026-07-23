package com.protrack.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AnalysisJobHandler#providerOf(String)} — pure classification logic, no
 * Spring context. Regression coverage for a bug found during the Gemini integration validation:
 * the original implementation only recognized Claude model names and fell back to returning the
 * raw model string as the "provider" for anything else, so Gemini results were persisted with
 * {@code provider} set to e.g. "gemini-2.5-flash" instead of "gemini".
 */
class AnalysisJobHandlerTest {

	@Test
	void classifiesClaudeModels() {
		assertThat(AnalysisJobHandler.providerOf("claude-sonnet-4-6")).isEqualTo("claude");
	}

	@Test
	void classifiesGeminiModels() {
		assertThat(AnalysisJobHandler.providerOf("gemini-2.5-flash")).isEqualTo("gemini");
		assertThat(AnalysisJobHandler.providerOf("gemini-flash-latest")).isEqualTo("gemini");
	}

	@Test
	void classifiesMock() {
		assertThat(AnalysisJobHandler.providerOf("mock")).isEqualTo("mock");
	}

	@Test
	void fallsBackToUnknownForNullModel() {
		assertThat(AnalysisJobHandler.providerOf(null)).isEqualTo("unknown");
	}

	@Test
	void passesThroughAnyOtherModelNameUnchanged() {
		// Not a recognized provider prefix — preserved as-is rather than guessed, matching the
		// pre-existing fallback behavior for unrecognized values.
		assertThat(AnalysisJobHandler.providerOf("some-future-model")).isEqualTo("some-future-model");
	}
}
