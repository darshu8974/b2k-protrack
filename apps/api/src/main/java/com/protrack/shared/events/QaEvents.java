package com.protrack.shared.events;

import java.util.UUID;

/**
 * Domain events published by the qa module and consumed by cross-cutting subscribers (audit).
 * Published within the originating transaction so audit rows are atomic with the change (matching
 * the project/file/package/ai event convention).
 */
public final class QaEvents {

	private QaEvents() {
	}

	/** A QA reviewer recorded a triage decision on a preflight issue. */
	public record IssueDecided(UUID organizationId, UUID projectId, UUID actorId, UUID issueId,
			String decision) {
	}

	/** A QA reviewer formally signed off (approved or rejected) — the atomic completion gate. */
	public record QaSignedOff(UUID organizationId, UUID projectId, UUID actorId, UUID signoffId,
			String decision, Integer qualityScore, String targetStage) {
	}
}
