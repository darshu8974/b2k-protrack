package com.protrack.audit.repository;

import com.protrack.audit.domain.AuditEvent;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Reusable {@link Specification}s for filtering the admin audit log. */
public final class AuditEventSpecifications {

	private AuditEventSpecifications() {
	}

	public static Specification<AuditEvent> inOrganization(UUID organizationId) {
		return (root, query, cb) -> cb.equal(root.get("organizationId"), organizationId);
	}

	public static Specification<AuditEvent> forProject(UUID projectId) {
		return (root, query, cb) -> cb.equal(root.get("projectId"), projectId);
	}

	public static Specification<AuditEvent> hasEventType(String eventType) {
		return (root, query, cb) -> cb.equal(root.get("eventType"), eventType);
	}

	public static Specification<AuditEvent> createdFrom(Instant from) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
	}

	public static Specification<AuditEvent> createdTo(Instant to) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
	}
}
