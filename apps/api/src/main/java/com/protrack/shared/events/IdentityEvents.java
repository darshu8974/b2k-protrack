package com.protrack.shared.events;

import java.util.UUID;

/**
 * Domain events for administrator user-management actions (API Specification §3.2). Published so
 * these land in the same audit trail as project/file/AI events — account creation, deletion, and
 * role/status changes are exactly the kind of action a SOC 2 / 21 CFR Part 11 audit trail must
 * cover, and were previously invisible to it entirely.
 */
public final class IdentityEvents {

	private IdentityEvents() {
	}

	public record UserCreated(UUID organizationId, UUID actorId, UUID userId, String email) {
	}

	public record UserDeleted(UUID organizationId, UUID actorId, UUID userId, String email) {
	}

	public record UserStatusChanged(UUID organizationId, UUID actorId, UUID userId, String status) {
	}

	public record RoleAssigned(UUID organizationId, UUID actorId, UUID userId, String roleCode) {
	}

	public record RoleRevoked(UUID organizationId, UUID actorId, UUID userId, String roleCode) {
	}
}
