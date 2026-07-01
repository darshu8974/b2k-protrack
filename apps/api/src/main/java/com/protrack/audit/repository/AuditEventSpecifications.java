package com.protrack.audit.repository;

import com.protrack.audit.domain.AuditEvent;
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
}
