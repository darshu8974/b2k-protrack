package com.protrack.shared.events;

import java.util.UUID;

/**
 * Domain events published by business modules and consumed by cross-cutting subscribers (audit).
 * Published within the originating transaction so audit rows are atomic with the change.
 */
public final class ProjectEvents {

	private ProjectEvents() {
	}

	public record ProjectCreated(UUID organizationId, UUID projectId, UUID actorId, String title) {
	}

	public record ProjectUpdated(UUID organizationId, UUID projectId, UUID actorId) {
	}

	public record ProjectMembersAssigned(UUID organizationId, UUID projectId, UUID actorId,
			int memberCount) {
	}

	public record ProjectStageChanged(UUID organizationId, UUID projectId, UUID actorId,
			String fromStage, String toStage, String triggeredRole) {
	}
}
