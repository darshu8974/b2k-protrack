package com.protrack.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PreflightJobHandler#providerOf(String)} — mirrors
 * {@link AnalysisJobHandlerTest}; the same Claude-only classification bug was duplicated here.
 */
class PreflightJobHandlerTest {

	@Test
	void classifiesClaudeModels() {
		assertThat(PreflightJobHandler.providerOf("claude-sonnet-4-6")).isEqualTo("claude");
	}

	@Test
	void classifiesGeminiModels() {
		assertThat(PreflightJobHandler.providerOf("gemini-2.5-flash")).isEqualTo("gemini");
		assertThat(PreflightJobHandler.providerOf("gemini-flash-latest")).isEqualTo("gemini");
	}

	@Test
	void classifiesMock() {
		assertThat(PreflightJobHandler.providerOf("mock")).isEqualTo("mock");
	}

	@Test
	void fallsBackToUnknownForNullModel() {
		assertThat(PreflightJobHandler.providerOf(null)).isEqualTo("unknown");
	}

	@Test
	void passesThroughAnyOtherModelNameUnchanged() {
		assertThat(PreflightJobHandler.providerOf("some-future-model")).isEqualTo("some-future-model");
	}
}
