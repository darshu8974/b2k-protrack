package com.protrack.identity.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

/** Bulk status change for several users at once (API Specification §3.2, "bulk deactivate"). */
public record BulkUserRequest(
		@NotNull @Pattern(regexp = "ACTIVATE|DEACTIVATE") String action,
		@NotEmpty List<UUID> userIds) {
}
