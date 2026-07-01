package com.protrack.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** An immutable audit record. Written only by the audit listener; never updated or deleted. */
@Entity
@Table(name = "audit_events")
public class AuditEvent {

	@Id
	private UUID id;

	@Column(name = "organization_id")
	private UUID organizationId;

	@Column(name = "project_id")
	private UUID projectId;

	@Column(name = "actor_id")
	private UUID actorId;

	@Column(name = "actor_type", nullable = false)
	private String actorType;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "entity_type", nullable = false)
	private String entityType;

	@Column(name = "entity_id")
	private UUID entityId;

	@Column(nullable = false)
	private String summary;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private String metadata;

	@Column(name = "correlation_id")
	private String correlationId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	protected AuditEvent() {
	}

	public AuditEvent(UUID id, UUID organizationId, UUID projectId, UUID actorId, String actorType,
			String eventType, String entityType, UUID entityId, String summary, String metadata,
			String correlationId) {
		this.id = id;
		this.organizationId = organizationId;
		this.projectId = projectId;
		this.actorId = actorId;
		this.actorType = actorType;
		this.eventType = eventType;
		this.entityType = entityType;
		this.entityId = entityId;
		this.summary = summary;
		this.metadata = metadata;
		this.correlationId = correlationId;
	}

	public UUID getId() {
		return id;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public UUID getActorId() {
		return actorId;
	}

	public String getActorType() {
		return actorType;
	}

	public String getEventType() {
		return eventType;
	}

	public String getEntityType() {
		return entityType;
	}

	public UUID getEntityId() {
		return entityId;
	}

	public String getSummary() {
		return summary;
	}

	public String getMetadata() {
		return metadata;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
