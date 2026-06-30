package com.protrack.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/** A fine-grained capability grant. Reference data (populated as RBAC permissions are defined). */
@Entity
@Table(name = "permissions")
public class Permission {

	@Id
	private Integer id;

	@Column(nullable = false, unique = true)
	private String code;

	private String description;

	protected Permission() {
	}

	public Integer getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Permission other)) {
			return false;
		}
		return id != null && id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
