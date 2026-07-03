package com.protrack.ai.client.dto;

/**
 * Request body sent to the FastAPI {@code /analyze/manuscript} endpoint. {@code fileUrl} is a
 * short-lived signed URL the stateless AI service fetches (it never resolves storage keys itself).
 */
public record AnalysisRequest(
		String jobId,
		String fileUrl,
		String docType,
		ProjectContextDto projectContext) {

	public record ProjectContextDto(
			String projectId,
			String title,
			String publicationType,
			String discipline) {
	}
}
