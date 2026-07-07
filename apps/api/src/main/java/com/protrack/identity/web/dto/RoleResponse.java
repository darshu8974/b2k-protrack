package com.protrack.identity.web.dto;

import com.protrack.identity.domain.Role;

/** A role reference row (API Specification §3.2, GET /roles). */
public record RoleResponse(Integer id, String code, String name, String description) {

	public static RoleResponse from(Role role) {
		return new RoleResponse(role.getId(), role.getCode(), role.getName(), role.getDescription());
	}
}
