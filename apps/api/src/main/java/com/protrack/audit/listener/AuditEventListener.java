package com.protrack.audit.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.protrack.audit.domain.AuditEvent;
import com.protrack.audit.repository.AuditEventRepository;
import com.protrack.shared.events.AiEvents;
import com.protrack.shared.events.FileEvents;
import com.protrack.shared.events.PackageEvents;
import com.protrack.shared.events.ProjectEvents;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Subscribes to domain events and writes immutable audit rows. Runs synchronously within the
 * publishing transaction, so an audit row exists exactly when the business change commits.
 */
@Component
public class AuditEventListener {

	private static final String ENTITY_PROJECT = "PROJECT";
	private static final String ENTITY_FILE = "FILE";
	private static final String ENTITY_PACKAGE = "PACKAGE";
	private static final String ENTITY_AI_JOB = "AI_JOB";
	private static final String ACTOR_USER = "USER";

	private final AuditEventRepository auditEventRepository;
	private final ObjectMapper objectMapper;

	public AuditEventListener(AuditEventRepository auditEventRepository, ObjectMapper objectMapper) {
		this.auditEventRepository = auditEventRepository;
		this.objectMapper = objectMapper;
	}

	@EventListener
	public void onProjectCreated(ProjectEvents.ProjectCreated event) {
		record(event.organizationId(), event.projectId(), event.actorId(), "PROJECT_CREATED",
				"Project created", Map.of("title", event.title()));
	}

	@EventListener
	public void onProjectUpdated(ProjectEvents.ProjectUpdated event) {
		record(event.organizationId(), event.projectId(), event.actorId(), "PROJECT_UPDATED",
				"Project details updated", null);
	}

	@EventListener
	public void onMembersAssigned(ProjectEvents.ProjectMembersAssigned event) {
		record(event.organizationId(), event.projectId(), event.actorId(), "MEMBERS_ASSIGNED",
				"Team updated (%d member(s))".formatted(event.memberCount()),
				Map.of("memberCount", event.memberCount()));
	}

	@EventListener
	public void onStageChanged(ProjectEvents.ProjectStageChanged event) {
		record(event.organizationId(), event.projectId(), event.actorId(), "STAGE_CHANGED",
				"%s → %s".formatted(event.fromStage(), event.toStage()),
				Map.of("fromStage", event.fromStage(), "toStage", event.toStage(),
						"role", event.triggeredRole()));
	}

	@EventListener
	public void onFileUploaded(FileEvents.FileUploaded event) {
		save(event.organizationId(), event.projectId(), event.actorId(), "FILE_UPLOADED",
				ENTITY_FILE, event.documentId(),
				"%s uploaded (v%d)".formatted(event.fileName(), event.versionNo()),
				Map.of("documentId", event.documentId().toString(),
						"versionId", event.versionId().toString(),
						"docType", event.docType(), "fileName", event.fileName(),
						"versionNo", event.versionNo()));
	}

	@EventListener
	public void onPackageAssembled(PackageEvents.PackageAssembled event) {
		save(event.organizationId(), event.projectId(), event.actorId(), "PACKAGE_ASSEMBLED",
				ENTITY_PACKAGE, event.packageId(),
				"Production package assembled (%d item(s))".formatted(event.itemCount()),
				Map.of("packageId", event.packageId().toString(), "itemCount", event.itemCount(),
						"totalSizeBytes", event.totalSizeBytes()));
	}

	@EventListener
	public void onAiJobStarted(AiEvents.AiJobStarted event) {
		save(event.organizationId(), event.projectId(), event.actorId(), "ANALYSIS_STARTED",
				ENTITY_AI_JOB, event.jobId(), "AI analysis started",
				Map.of("jobId", event.jobId().toString(), "jobType", event.jobType()));
	}

	@EventListener
	public void onAnalysisCompleted(AiEvents.AnalysisCompleted event) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("jobId", event.jobId().toString());
		metadata.put("analysisResultId", event.analysisResultId().toString());
		metadata.put("overallConfidence", event.overallConfidence());
		save(event.organizationId(), event.projectId(), event.actorId(), "ANALYSIS_COMPLETED",
				ENTITY_AI_JOB, event.jobId(), "AI analysis completed", metadata);
	}

	@EventListener
	public void onAiJobFailed(AiEvents.AiJobFailed event) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("jobId", event.jobId().toString());
		metadata.put("jobType", event.jobType());
		metadata.put("error", event.errorMessage());
		save(event.organizationId(), event.projectId(), event.actorId(), "ANALYSIS_FAILED",
				ENTITY_AI_JOB, event.jobId(), "AI job failed", metadata);
	}

	private void record(UUID organizationId, UUID projectId, UUID actorId, String eventType,
			String summary, Map<String, ?> metadata) {
		save(organizationId, projectId, actorId, eventType, ENTITY_PROJECT, projectId, summary, metadata);
	}

	private void save(UUID organizationId, UUID projectId, UUID actorId, String eventType,
			String entityType, UUID entityId, String summary, Map<String, ?> metadata) {
		auditEventRepository.save(new AuditEvent(
				UUID.randomUUID(), organizationId, projectId, actorId, ACTOR_USER, eventType,
				entityType, entityId, summary, toJson(metadata), MDC.get("traceId")));
	}

	private String toJson(Map<String, ?> metadata) {
		if (metadata == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(metadata);
		} catch (Exception ex) {
			return null;
		}
	}
}
