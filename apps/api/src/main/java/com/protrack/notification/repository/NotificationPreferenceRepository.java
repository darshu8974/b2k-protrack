package com.protrack.notification.repository;

import com.protrack.notification.domain.NotificationPreference;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link NotificationPreference} (per-user, per-type channel opt-in). */
public interface NotificationPreferenceRepository
		extends JpaRepository<NotificationPreference, UUID> {

	List<NotificationPreference> findByUserId(UUID userId);

	Optional<NotificationPreference> findByUserIdAndType(UUID userId, String type);

	/** Preferences of several users for one type — the fan-out lookup. */
	List<NotificationPreference> findByUserIdInAndType(Collection<UUID> userIds, String type);
}
