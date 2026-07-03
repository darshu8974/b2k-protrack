package com.protrack.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link AiJob} lifecycle (no Spring context, no Docker). */
class AiJobTest {

	private AiJob newJob() {
		return new AiJob(UUID.randomUUID(), UUID.randomUUID(), JobType.MANUSCRIPT_ANALYSIS,
				UUID.randomUUID(), UUID.randomUUID());
	}

	@Test
	void startsQueuedAtZero() {
		AiJob job = newJob();
		assertThat(job.getStatus()).isEqualTo("QUEUED");
		assertThat(job.getProgressPct()).isZero();
		assertThat(job.getJobType()).isEqualTo("MANUSCRIPT_ANALYSIS");
	}

	@Test
	void markRunningSetsStartedAt() {
		AiJob job = newJob();
		job.markRunning();
		assertThat(job.getStatus()).isEqualTo("RUNNING");
		assertThat(job.getStartedAt()).isNotNull();
	}

	@Test
	void updateProgressAdvancesPercentAndStatus() {
		AiJob job = newJob();
		job.updateProgress(40, JobStatus.RUNNING);
		assertThat(job.getProgressPct()).isEqualTo(40);
		assertThat(job.getStatus()).isEqualTo("RUNNING");
	}

	@Test
	void markSucceededCompletesTo100WithProviderAndDuration() {
		AiJob job = newJob();
		job.markRunning();
		job.markSucceeded("mock", "mock");
		assertThat(job.getStatus()).isEqualTo("SUCCEEDED");
		assertThat(job.getProgressPct()).isEqualTo(100);
		assertThat(job.getProvider()).isEqualTo("mock");
		assertThat(job.getModel()).isEqualTo("mock");
		assertThat(job.getFinishedAt()).isNotNull();
		assertThat(job.getDurationMs()).isNotNull().isGreaterThanOrEqualTo(0);
	}

	@Test
	void markFailedRecordsError() {
		AiJob job = newJob();
		job.markRunning();
		job.markFailed("AI service unreachable");
		assertThat(job.getStatus()).isEqualTo("FAILED");
		assertThat(job.getErrorMessage()).isEqualTo("AI service unreachable");
		assertThat(job.getFinishedAt()).isNotNull();
	}
}
