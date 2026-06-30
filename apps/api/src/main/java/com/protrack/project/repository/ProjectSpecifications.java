package com.protrack.project.repository;

import com.protrack.project.domain.Project;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Reusable {@link Specification}s for filtering the project list. */
public final class ProjectSpecifications {

	private ProjectSpecifications() {
	}

	public static Specification<Project> notDeleted() {
		return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
	}

	public static Specification<Project> inOrganization(UUID organizationId) {
		return (root, query, cb) -> cb.equal(root.get("organizationId"), organizationId);
	}

	public static Specification<Project> hasStage(String stage) {
		return (root, query, cb) -> cb.equal(root.get("currentStage"), stage);
	}

	public static Specification<Project> hasStatus(String status) {
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}

	public static Specification<Project> hasPriority(String priority) {
		return (root, query, cb) -> cb.equal(root.get("priority"), priority);
	}

	public static Specification<Project> hasImprint(UUID imprintId) {
		return (root, query, cb) -> cb.equal(root.get("imprint").get("id"), imprintId);
	}

	public static Specification<Project> ownedBy(UUID ownerId) {
		return (root, query, cb) -> cb.equal(root.get("ownerId"), ownerId);
	}

	/** Case-insensitive match on title or ISBN. */
	public static Specification<Project> search(String term) {
		String like = "%" + term.toLowerCase() + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("title")), like),
				cb.like(cb.lower(root.get("isbn")), like));
	}
}
