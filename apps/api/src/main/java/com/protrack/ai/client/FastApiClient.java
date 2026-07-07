package com.protrack.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.protrack.ai.client.dto.AnalysisRequest;
import com.protrack.ai.client.dto.AnalysisResponse;
import com.protrack.ai.client.dto.AssistantChatRequest;
import com.protrack.ai.client.dto.AssistantChatResponse;
import com.protrack.ai.client.dto.PreflightRequest;
import com.protrack.ai.client.dto.PreflightResponse;
import com.protrack.shared.properties.ProtrackProperties;
import com.protrack.shared.web.CorrelationIdFilter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * {@link AiServiceClient} adapter over Spring {@link RestClient}, guarded by Resilience4j
 * (retry + circuit breaker, instance {@code aiService}). Every call carries the shared internal key
 * and propagates the trace id for end-to-end correlation. 5xx/connection failures become
 * retryable {@link AiServiceUnavailableException}; 4xx become permanent {@link AiServiceException}.
 */
@Component
public class FastApiClient implements AiServiceClient {

	private static final String INTERNAL_KEY_HEADER = "X-Internal-Key";
	private static final int CONNECT_TIMEOUT_MS = 5_000;

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final String internalKey;

	public FastApiClient(ProtrackProperties properties, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.internalKey = properties.ai().internalKey();

		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
		factory.setReadTimeout((int) Math.min(Integer.MAX_VALUE, properties.ai().timeoutMs()));
		this.restClient = RestClient.builder()
				.baseUrl(properties.ai().baseUrl())
				.requestFactory(factory)
				.build();
	}

	@Override
	@Retry(name = "aiService")
	@CircuitBreaker(name = "aiService")
	public AiAnalysisResult analyzeManuscript(AnalysisRequest request) {
		String body = post("/internal/v1/analyze/manuscript", request);
		try {
			AnalysisResponse parsed = objectMapper.readValue(body, AnalysisResponse.class);
			return new AiAnalysisResult(parsed, body);
		} catch (Exception ex) {
			throw new AiServiceException("Could not parse AI service response", ex);
		}
	}

	@Override
	@Retry(name = "aiService")
	@CircuitBreaker(name = "aiService")
	public AiPreflightResult preflightPdf(PreflightRequest request) {
		String body = post("/internal/v1/preflight/pdf", request);
		try {
			PreflightResponse parsed = objectMapper.readValue(body, PreflightResponse.class);
			return new AiPreflightResult(parsed, body);
		} catch (Exception ex) {
			throw new AiServiceException("Could not parse AI service response", ex);
		}
	}

	@Override
	@Retry(name = "aiService")
	@CircuitBreaker(name = "aiService")
	public AssistantChatResponse assistantChat(AssistantChatRequest request) {
		String body = post("/internal/v1/assistant/chat", request);
		try {
			return objectMapper.readValue(body, AssistantChatResponse.class);
		} catch (Exception ex) {
			throw new AiServiceException("Could not parse AI service response", ex);
		}
	}

	/** POST a JSON body to the AI service with the internal key + trace id; return the raw body. */
	private String post(String uri, Object requestBody) {
		String traceId = MDC.get(CorrelationIdFilter.MDC_KEY);
		try {
			return restClient.post()
					.uri(uri)
					.header(INTERNAL_KEY_HEADER, internalKey)
					.headers(headers -> {
						if (traceId != null) {
							headers.set(CorrelationIdFilter.HEADER, traceId);
						}
					})
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestBody)
					.retrieve()
					.onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
						throw new AiServiceUnavailableException(
								"AI service returned " + res.getStatusCode());
					})
					.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
						throw new AiServiceException("AI service rejected the request: "
								+ res.getStatusCode());
					})
					.body(String.class);
		} catch (ResourceAccessException ex) {
			throw new AiServiceUnavailableException("AI service unreachable: " + ex.getMessage(), ex);
		}
	}
}
