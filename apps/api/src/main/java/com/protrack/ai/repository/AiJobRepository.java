package com.protrack.ai.repository;

import com.protrack.ai.domain.AiJob;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link AiJob}. */
public interface AiJobRepository extends JpaRepository<AiJob, UUID> {

	boolean existsByProjectIdAndJobTypeAndStatusIn(UUID projectId, String jobType, Collection<String> statuses);
}
