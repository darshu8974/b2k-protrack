package com.protrack.project.web.dto;

import com.protrack.project.domain.Priority;
import com.protrack.project.domain.PublicationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Body for POST /api/v1/projects. ISBN format/uniqueness is validated in the service. */
public record CreateProjectRequest(
		@NotBlank @Size(min = 3, max = 250) String title,
		String isbn,
		@NotNull UUID imprintId,
		@NotNull PublicationType publicationType,
		String discipline,
		@Size(max = 4000) String brief,
		@Positive Integer pageExtent,
		String trimSize,
		Priority priority,
		LocalDate dueDate,
		List<UUID> memberUserIds) {
}
