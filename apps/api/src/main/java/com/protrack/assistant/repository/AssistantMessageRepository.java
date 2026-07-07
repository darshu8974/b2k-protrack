package com.protrack.assistant.repository;

import com.protrack.assistant.domain.AssistantMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantMessageRepository extends JpaRepository<AssistantMessage, UUID> {

	/** A thread's messages in chronological order (the chat transcript). */
	List<AssistantMessage> findByThreadIdOrderByCreatedAtAsc(UUID threadId);
}
