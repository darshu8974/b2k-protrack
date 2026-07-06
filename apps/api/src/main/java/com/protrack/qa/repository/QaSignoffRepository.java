package com.protrack.qa.repository;

import com.protrack.qa.domain.QaSignoff;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link QaSignoff} (append-only e-signature attestations). */
public interface QaSignoffRepository extends JpaRepository<QaSignoff, UUID> {

	List<QaSignoff> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
