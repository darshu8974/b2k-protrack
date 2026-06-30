package com.protrack.identity.spi;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Published interface of the identity module for other modules to resolve users without reaching
 * into identity's entities/repositories directly (preserves module boundaries).
 */
public interface IdentityFacade {

	/** A lightweight user projection for display in other modules. */
	record UserBrief(UUID id, String fullName, String email, String avatarInitials, String avatarColor) {
	}

	boolean existsById(UUID userId);

	/** Returns the subset of the given ids that correspond to existing users. */
	Set<UUID> findExistingIds(Collection<UUID> userIds);

	Optional<UserBrief> findBrief(UUID userId);

	Map<UUID, UserBrief> findBriefs(Collection<UUID> userIds);

	/** The organization a user belongs to (used to scope newly created records). */
	Optional<UUID> findOrganizationId(UUID userId);
}
