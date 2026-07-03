package com.protrack.analysis.repository;

import com.protrack.analysis.domain.AnalysisResult;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link AnalysisResult}. Child rows load lazily within the read transaction. */
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {

	Optional<AnalysisResult> findFirstByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
