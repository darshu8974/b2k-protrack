package com.protrack.ai.client;

import com.protrack.ai.client.dto.AnalysisRequest;
import com.protrack.ai.client.dto.AnalysisResponse;

/**
 * Outbound port to the FastAPI AI service. Adapters ({@link FastApiClient}) handle transport,
 * the internal key, trace propagation, and resilience. Returns both the parsed response and the
 * raw JSON (kept as provenance in {@code analysis_results.raw_payload}).
 */
public interface AiServiceClient {

	/** Parsed response plus the raw JSON body for provenance. */
	record AiAnalysisResult(AnalysisResponse response, String rawPayload) {
	}

	AiAnalysisResult analyzeManuscript(AnalysisRequest request);
}
