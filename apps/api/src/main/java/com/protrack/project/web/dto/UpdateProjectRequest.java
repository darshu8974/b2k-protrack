package com.protrack.project.web.dto;

import com.protrack.project.domain.Priority;
import com.protrack.project.domain.ProjectStatus;
import com.protrack.project.domain.PublicationType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Body for PATCH /api/v1/projects/{id}. All fields optional — only non-null fields are applied.
 * Status changes to COMPLETED are not allowed here (that happens via workflow, a later task).
 */
public record UpdateProjectRequest(
		@Size(min = 3, max = 250) String title,
		String isbn,
		UUID imprintId,
		PublicationType publicationType,
		String discipline,
		@Size(max = 4000) String brief,
		@Positive Integer pageExtent,
		String trimSize,
		Priority priority,
		ProjectStatus status,
		LocalDate dueDate) {
}
