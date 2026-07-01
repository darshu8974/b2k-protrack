package com.protrack.shared.events;

import java.util.UUID;

/**
 * Domain events published by the files module and consumed by cross-cutting subscribers (audit).
 * Published within the originating transaction so audit rows are atomic with the change.
 */
public final class FileEvents {

	private FileEvents() {
	}

	/** A new file version was stored (either a brand-new document or a subsequent version). */
	public record FileUploaded(UUID organizationId, UUID projectId, UUID actorId, UUID documentId,
			UUID versionId, String docType, String fileName, int versionNo) {
	}
}
