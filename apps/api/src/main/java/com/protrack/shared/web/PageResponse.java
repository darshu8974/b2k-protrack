package com.protrack.shared.web;

import java.util.List;
import org.springframework.data.domain.Page;

/** Standard paginated list envelope (REST API Specification §1.3). */
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		String sort,
		boolean hasNext) {

	public static <T> PageResponse<T> of(Page<T> page) {
		return new PageResponse<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.getSort().toString(),
				page.hasNext());
	}
}
