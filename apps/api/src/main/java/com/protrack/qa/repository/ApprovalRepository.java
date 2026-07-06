package com.protrack.qa.repository;

import com.protrack.qa.domain.Approval;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Approval} (append-only gate history). */
public interface ApprovalRepository extends JpaRepository<Approval, UUID> {

	List<Approval> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
