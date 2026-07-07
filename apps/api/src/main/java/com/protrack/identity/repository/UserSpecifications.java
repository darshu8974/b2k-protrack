package com.protrack.identity.repository;

import com.protrack.identity.domain.User;
import jakarta.persistence.criteria.Join;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Reusable {@link Specification}s for filtering the admin user directory. */
public final class UserSpecifications {

	private UserSpecifications() {
	}

	public static Specification<User> inOrganization(UUID organizationId) {
		return (root, query, cb) -> cb.equal(root.get("organizationId"), organizationId);
	}

	public static Specification<User> hasStatus(String status) {
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}

	/** Users holding the given role code (joins user_roles → roles). */
	public static Specification<User> hasRole(String roleCode) {
		return (root, query, cb) -> {
			if (query != null) {
				query.distinct(true);
			}
			Join<Object, Object> roles = root.join("roles");
			return cb.equal(roles.get("code"), roleCode);
		};
	}

	/** Case-insensitive match on full name or email. */
	public static Specification<User> search(String term) {
		String like = "%" + term.toLowerCase() + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("fullName")), like),
				cb.like(cb.lower(root.get("email")), like));
	}
}
