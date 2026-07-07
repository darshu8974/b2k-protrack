package com.protrack.identity.web.dto;

import com.protrack.identity.domain.Permission;

/** A permission reference row (API Specification §3.2, GET /permissions). */
public record PermissionResponse(Integer id, String code, String description) {

	public static PermissionResponse from(Permission permission) {
		return new PermissionResponse(permission.getId(), permission.getCode(),
				permission.getDescription());
	}
}
