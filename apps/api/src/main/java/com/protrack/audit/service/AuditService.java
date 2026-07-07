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
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
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

/** Reads the audit log (admin), the per-project activity feed, and the CSV export. */
@Service
public class AuditService {

	private static final int CSV_PAGE_SIZE = 500;
	private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

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

	/**
	 * Build the (org-scoped, filtered) audit log as CSV bytes. Fetches page-by-page to bound the
	 * query, accumulating into a buffer returned with a Content-Length (not chunked) so every client
	 * receives a complete response. Same filters as {@link #list} (project, eventType). Phase-1 audit
	 * volumes are modest; a true streaming export can be revisited when the log grows large.
	 */
	@Transactional(readOnly = true)
	public byte[] exportCsv(UUID currentUserId, UUID projectId, String eventType) {
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

		StringBuilder csv = new StringBuilder();
		writeRow(csv, "Time", "Event", "Entity Type", "Entity Id", "Summary", "Actor",
				"Actor Type", "Project Id", "Correlation Id");

		int pageNumber = 0;
		Page<AuditEvent> page;
		do {
			Pageable pageable = PageRequest.of(pageNumber, CSV_PAGE_SIZE,
					Sort.by(Sort.Direction.DESC, "createdAt"));
			page = auditEventRepository.findAll(spec, pageable);
			Map<UUID, UserBrief> actors = resolveActors(page.getContent());
			for (AuditEvent event : page.getContent()) {
				UserBrief actor = event.getActorId() == null ? null : actors.get(event.getActorId());
				writeRow(csv,
						event.getCreatedAt() == null ? "" : ISO.format(event.getCreatedAt()),
						event.getEventType(),
						event.getEntityType(),
						event.getEntityId() == null ? "" : event.getEntityId().toString(),
						event.getSummary(),
						actor == null ? "" : actor.fullName(),
						event.getActorType(),
						event.getProjectId() == null ? "" : event.getProjectId().toString(),
						event.getCorrelationId());
			}
			pageNumber++;
		} while (page.hasNext());

		return csv.toString().getBytes(StandardCharsets.UTF_8);
	}

	private static void writeRow(StringBuilder csv, String... cells) {
		for (int i = 0; i < cells.length; i++) {
			if (i > 0) {
				csv.append(',');
			}
			csv.append(csvEscape(cells[i]));
		}
		csv.append("\r\n");
	}

	/** RFC-4180 escaping: wrap in quotes when the value contains a comma, quote, or newline. */
	private static String csvEscape(String value) {
		if (value == null) {
			return "";
		}
		if (value.contains(",") || value.contains("\"") || value.contains("\n")
				|| value.contains("\r")) {
			return '"' + value.replace("\"", "\"\"") + '"';
		}
		return value;
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
