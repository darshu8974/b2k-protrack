package com.protrack.project.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Body for POST /api/v1/projects/{id}/members — assign (or update) one or more members. */
public record AssignMembersRequest(
		@NotEmpty @Valid List<MemberAssignment> members) {

	public record MemberAssignment(
			@NotNull UUID userId,
			String roleInProject,
			@DecimalMin("0") @DecimalMax("100") BigDecimal matchScore) {
	}
}
