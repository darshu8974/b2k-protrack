package com.protrack.assistant.repository;

import com.protrack.assistant.domain.AssistantThread;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantThreadRepository extends JpaRepository<AssistantThread, UUID> {

	/** The single thread for a (project, user) pair, if one exists (get-or-create keying). */
	Optional<AssistantThread> findByProjectIdAndUserId(UUID projectId, UUID userId);
}
