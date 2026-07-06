package com.protrack.qa.web.dto;

import java.time.Instant;

/** The result of a QA sign-off: the attestation plus the workflow stage the project moved to. */
public record SignoffResponse(
		String id,
		String projectId,
		String preflightRunId,
		String decision,
		Integer qualityScore,
		String signatureHash,
		String notes,
		String signedBy,
		String signedByName,
		String stage,
		Instant createdAt) {
}
