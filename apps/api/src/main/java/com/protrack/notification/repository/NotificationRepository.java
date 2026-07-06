package com.protrack.notification.repository;

import com.protrack.notification.domain.Notification;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for {@link Notification} (the per-recipient in-app feed). */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

	Page<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(UUID recipientId,
			Pageable pageable);

	long countByRecipientIdAndReadFalse(UUID recipientId);

	Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId);

	/** Bulk-mark every unread notification of a recipient as read; returns the number updated. */
	@Modifying
	@Query("""
			UPDATE Notification n SET n.read = true, n.readAt = :when
			WHERE n.recipientId = :recipientId AND n.read = false""")
	int markAllRead(@Param("recipientId") UUID recipientId, @Param("when") Instant when);
}
