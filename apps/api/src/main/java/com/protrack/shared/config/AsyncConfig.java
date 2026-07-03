package com.protrack.shared.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Enables asynchronous processing and defines the bounded pool used for long-running AI jobs.
 * The AI orchestration dispatches FastAPI calls onto {@code aiExecutor} so the request thread
 * returns immediately (202) while the job runs off the request path.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean("aiExecutor")
	public Executor aiExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(8);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("ai-job-");
		executor.initialize();
		return executor;
	}
}
