package com.protrack.ai.client.dto;

/**
 * Request body sent to the FastAPI {@code /preflight/pdf} endpoint. {@code fileUrl} is a short-lived
 * signed URL the stateless AI service fetches; {@code standard} is an optional target print standard
 * (e.g. {@code PDF/X-4}), null when unspecified.
 */
public record PreflightRequest(String jobId, String fileUrl, String standard) {
}
