package com.protrack.shared.events;

import java.util.UUID;

/**
 * Domain events published by the packaging module and consumed by cross-cutting subscribers (audit).
 * Published within the originating transaction so audit rows are atomic with the change.
 */
public final class PackageEvents {

	private PackageEvents() {
	}

	/** A production package was assembled (or re-assembled) for hand-off. */
	public record PackageAssembled(UUID organizationId, UUID projectId, UUID actorId, UUID packageId,
			int itemCount, long totalSizeBytes) {
	}
}
