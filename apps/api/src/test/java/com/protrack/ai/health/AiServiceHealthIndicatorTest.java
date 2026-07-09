package com.protrack.ai.health;

import static org.assertj.core.api.Assertions.assertThat;

import com.protrack.shared.properties.ProtrackProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

/**
 * Unit test for {@link AiServiceHealthIndicator}'s DOWN path (unreachable service). The UP path is
 * exercised in live end-to-end verification with the AI service running.
 */
class AiServiceHealthIndicatorTest {

	private static ProtrackProperties props(String aiBaseUrl) {
		return new ProtrackProperties(null, null,
				new ProtrackProperties.Ai(aiBaseUrl, "k", 1000L), null);
	}

	@Test
	void anUnreachableAiServiceReportsDownWithTheBaseUrl() {
		// Nothing listens on this port -> connection refused within the short probe timeout.
		Health health = new AiServiceHealthIndicator(props("http://localhost:59997")).health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsEntry("baseUrl", "http://localhost:59997");
		assertThat(health.getDetails()).containsKey("error");
	}
}
