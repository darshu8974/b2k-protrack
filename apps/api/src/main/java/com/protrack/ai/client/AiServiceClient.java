package com.protrack.ai.client;

import com.protrack.ai.client.dto.AnalysisRequest;
import com.protrack.ai.client.dto.AnalysisResponse;
import com.protrack.ai.client.dto.PreflightRequest;
import com.protrack.ai.client.dto.PreflightResponse;

/**
 * Outbound port to the FastAPI AI service. Adapters ({@link FastApiClient}) handle transport,
 * the internal key, trace propagation, and resilience. Returns both the parsed response and the
 * raw JSON (kept as provenance in the {@code raw_payload} columns).
 */
public interface AiServiceClient {

	/** Parsed analysis response plus the raw JSON body for provenance. */
	record AiAnalysisResult(AnalysisResponse response, String rawPayload) {
	}

	/** Parsed preflight response plus the raw JSON body for provenance. */
	record AiPreflightResult(PreflightResponse response, String rawPayload) {
	}

	AiAnalysisResult analyzeManuscript(AnalysisRequest request);

	AiPreflightResult preflightPdf(PreflightRequest request);
}
