package com.protrack.project.web.dto;

import java.time.LocalDate;

/** Lightweight project row for the list endpoint. */
public record ProjectSummaryResponse(
		String id,
		String title,
		String isbn,
		String publicationType,
		String discipline,
		String imprintName,
		String currentStage,
		String status,
		String priority,
		LocalDate dueDate,
		String ownerName) {
}
