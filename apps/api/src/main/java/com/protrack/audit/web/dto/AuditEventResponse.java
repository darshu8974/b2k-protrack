package com.protrack.audit.web.dto;

import java.time.Instant;

/** A single audit-log entry with the actor resolved and metadata parsed. */
public record AuditEventResponse(
		String id,
		String eventType,
		String entityType,
		String entityId,
		String summary,
		String actorId,
		String actorName,
		String actorType,
		String projectId,
		Object metadata,
		String correlationId,
		Instant createdAt) {
}
