package com.protrack.audit.web;

import com.protrack.audit.service.AuditService;
import com.protrack.audit.web.dto.AuditEventResponse;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.web.PageResponse;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Audit endpoints: the org-wide log (admin only) and a per-project activity feed (any member/role).
 */
@RestController
@RequestMapping("/api/v1")
public class AuditController {

	private static final int ACTIVITY_LIMIT = 50;

	private final AuditService auditService;

	public AuditController(AuditService auditService) {
		this.auditService = auditService;
	}

	@GetMapping("/audit-events")
	@PreAuthorize("hasRole('ADMIN')")
	public PageResponse<AuditEventResponse> list(
			@RequestParam(required = false) UUID projectId,
			@RequestParam(required = false) String eventType,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
			Principal principal) {
		return auditService.list(UUID.fromString(principal.getName()), projectId, eventType, pageable);
	}

	@GetMapping("/projects/{id}/activity")
	public List<AuditEventResponse> activity(@PathVariable UUID id) {
		return auditService.projectActivity(id, ACTIVITY_LIMIT);
	}

	/** CSV export of the (org-scoped, filtered) audit log — administrators only (API Spec §3.14). */
	@GetMapping("/audit-events:export")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<byte[]> export(
			@RequestParam(required = false, defaultValue = "csv") String format,
			@RequestParam(required = false) UUID projectId,
			@RequestParam(required = false) String eventType,
			Principal principal) {
		if (!"csv".equalsIgnoreCase(format)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_FORMAT",
					"Only format=csv is supported.");
		}
		UUID actorId = UUID.fromString(principal.getName());
		byte[] csv = auditService.exportCsv(actorId, projectId, eventType);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.csv\"")
				.contentType(MediaType.parseMediaType("text/csv"))
				.body(csv);
	}
}
