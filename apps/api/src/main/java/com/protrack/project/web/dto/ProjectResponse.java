package com.protrack.project.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Full project detail returned by create/get/update. */
public record ProjectResponse(
		String id,
		String title,
		String isbn,
		String publicationType,
		String discipline,
		String brief,
		Integer pageExtent,
		String trimSize,
		String priority,
		String currentStage,
		String status,
		LocalDate dueDate,
		LocalDate createdDate,
		Instant createdAt,
		Instant updatedAt,
		ImprintResponse imprint,
		OwnerResponse owner,
		List<ProjectMemberResponse> members) {

	/** Project owner reference. */
	public record OwnerResponse(String id, String fullName, String email) {
	}
}
