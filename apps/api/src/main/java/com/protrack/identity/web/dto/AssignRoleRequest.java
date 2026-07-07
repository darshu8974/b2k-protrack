package com.protrack.identity.web.dto;

import jakarta.validation.constraints.NotNull;

/** Assign a role to a user (API Specification §3.2). */
public record AssignRoleRequest(@NotNull Integer roleId) {
}
