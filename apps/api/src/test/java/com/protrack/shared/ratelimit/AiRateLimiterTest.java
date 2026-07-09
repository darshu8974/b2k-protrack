package com.protrack.shared.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.protrack.shared.error.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Unit tests for {@link AiRateLimiter} — per-user budgets, fail-fast 429 (no Docker). */
class AiRateLimiterTest {

	@Test
	void allowsUpToTheLimitThenRejectsWith429() {
		AiRateLimiter limiter = new AiRateLimiter(3);
		String user = "user-a";

		limiter.check(user);
		limiter.check(user);
		limiter.check(user); // 3 permits used within the window

		assertThatThrownBy(() -> limiter.check(user))
				.isInstanceOfSatisfying(ApiException.class, ex -> {
					assertThat(ex.getCode()).isEqualTo("RATE_LIMITED");
					assertThat(ex.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
				});
	}

	@Test
	void limitsAreIsolatedPerUser() {
		AiRateLimiter limiter = new AiRateLimiter(1);

		limiter.check("alice"); // alice exhausts her single permit
		assertThatThrownBy(() -> limiter.check("alice")).isInstanceOf(ApiException.class);

		// bob has his own independent budget and is unaffected.
		assertThatCode(() -> limiter.check("bob")).doesNotThrowAnyException();
	}

	@Test
	void aConfiguredLimitBelowOneIsTreatedAsOne() {
		AiRateLimiter limiter = new AiRateLimiter(0);
		assertThatCode(() -> limiter.check("u")).doesNotThrowAnyException();
		assertThatThrownBy(() -> limiter.check("u")).isInstanceOf(ApiException.class);
	}
}
