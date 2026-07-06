package com.protrack.shared.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Enables asynchronous processing and defines the bounded pools used off the request path.
 * The AI orchestration dispatches FastAPI calls onto {@code aiExecutor} so the request thread
 * returns immediately (202) while the job runs; the notification module sends email on
 * {@code notificationExecutor} so SMTP latency never blocks a request or event listener.
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

	@Bean("notificationExecutor")
	public Executor notificationExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("notify-");
		executor.initialize();
		return executor;
	}
}
