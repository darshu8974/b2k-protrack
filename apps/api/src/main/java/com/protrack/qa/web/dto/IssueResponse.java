package com.protrack.qa.web.dto;

import com.protrack.preflight.spi.PreflightFacade.IssueRef;
import java.time.Instant;

/** A QA issue on the triage surface. */
public record IssueResponse(
		String id,
		String projectId,
		String preflightRunId,
		String category,
		String severity,
		String title,
		String recommendation,
		String pageRef,
		String source,
		String status,
		Instant createdAt) {

	public static IssueResponse from(IssueRef ref) {
		return new IssueResponse(
				ref.id().toString(), ref.projectId().toString(), ref.preflightRunId().toString(),
				ref.category(), ref.severity(), ref.title(), ref.recommendation(), ref.pageRef(),
				ref.source(), ref.status(), ref.createdAt());
	}
}
