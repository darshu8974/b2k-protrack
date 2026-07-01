package com.protrack.audit.repository;

import com.protrack.audit.domain.AuditEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Data access for the append-only {@link AuditEvent} (insert + query only). */
public interface AuditEventRepository
		extends JpaRepository<AuditEvent, UUID>, JpaSpecificationExecutor<AuditEvent> {

	List<AuditEvent> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);
}
