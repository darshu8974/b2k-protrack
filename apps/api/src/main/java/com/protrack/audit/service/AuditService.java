package com.protrack.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.protrack.audit.domain.AuditEvent;
import com.protrack.audit.repository.AuditEventRepository;
import com.protrack.audit.repository.AuditEventSpecifications;
import com.protrack.audit.web.dto.AuditEventResponse;
import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.web.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Reads the audit log (admin) and the per-project activity feed. */
@Service
public class AuditService {

	private final AuditEventRepository auditEventRepository;
	private final IdentityFacade identityFacade;
	private final ObjectMapper objectMapper;

	public AuditService(AuditEventRepository auditEventRepository, IdentityFacade identityFacade,
			ObjectMapper objectMapper) {
		this.auditEventRepository = auditEventRepository;
		this.identityFacade = identityFacade;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public PageResponse<AuditEventResponse> list(UUID currentUserId, UUID projectId, String eventType,
			Pageable pageable) {
		UUID organizationId = identityFacade.findOrganizationId(currentUserId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
						"Authenticated user no longer exists."));

		Specification<AuditEvent> spec = Specification
				.where(AuditEventSpecifications.inOrganization(organizationId));
		if (projectId != null) {
			spec = spec.and(AuditEventSpecifications.forProject(projectId));
		}
		if (StringUtils.hasText(eventType)) {
			spec = spec.and(AuditEventSpecifications.hasEventType(eventType));
		}

		Page<AuditEvent> page = auditEventRepository.findAll(spec, pageable);
		Map<UUID, UserBrief> actors = resolveActors(page.getContent());
		return PageResponse.of(page.map(event -> toResponse(event, actors)));
	}

	@Transactional(readOnly = true)
	public List<AuditEventResponse> projectActivity(UUID projectId, int limit) {
		List<AuditEvent> events = auditEventRepository.findByProjectIdOrderByCreatedAtDesc(
				projectId, PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")));
		Map<UUID, UserBrief> actors = resolveActors(events);
		return events.stream().map(event -> toResponse(event, actors)).toList();
	}

	private Map<UUID, UserBrief> resolveActors(List<AuditEvent> events) {
		Set<UUID> actorIds = events.stream()
				.map(AuditEvent::getActorId).filter(Objects::nonNull).collect(Collectors.toSet());
		return identityFacade.findBriefs(actorIds);
	}

	private AuditEventResponse toResponse(AuditEvent event, Map<UUID, UserBrief> actors) {
		UserBrief actor = event.getActorId() == null ? null : actors.get(event.getActorId());
		return new AuditEventResponse(
				event.getId().toString(),
				event.getEventType(),
				event.getEntityType(),
				event.getEntityId() == null ? null : event.getEntityId().toString(),
				event.getSummary(),
				event.getActorId() == null ? null : event.getActorId().toString(),
				actor == null ? null : actor.fullName(),
				event.getActorType(),
				event.getProjectId() == null ? null : event.getProjectId().toString(),
				parseMetadata(event.getMetadata()),
				event.getCorrelationId(),
				event.getCreatedAt());
	}

	private Object parseMetadata(String metadata) {
		if (metadata == null) {
			return null;
		}
		try {
			return objectMapper.readTree(metadata);
		} catch (Exception ex) {
			return metadata;
		}
	}
}
