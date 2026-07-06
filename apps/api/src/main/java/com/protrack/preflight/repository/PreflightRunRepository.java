package com.protrack.preflight.repository;

import com.protrack.preflight.domain.PreflightRun;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link PreflightRun}. Child checks load lazily within the read transaction. */
public interface PreflightRunRepository extends JpaRepository<PreflightRun, UUID> {

	Optional<PreflightRun> findFirstByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
