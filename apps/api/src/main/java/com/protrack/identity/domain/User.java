package com.protrack.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A platform user account. Mapped read/update for authentication; ids and timestamps are managed
 * outside this entity (app-generated UUID v7 on create, DB defaults for audit columns).
 */
@Entity
@Table(name = "users")
public class User {

	@Id
	private UUID id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "full_name", nullable = false)
	private String fullName;

	@Column(name = "avatar_initials")
	private String avatarInitials;

	@Column(name = "avatar_color")
	private String avatarColor;

	@Column(nullable = false)
	private String status;

	@Column(name = "last_login_at")
	private Instant lastLoginAt;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	protected User() {
	}

	/**
	 * Create a new account (administrator user creation). Id is app-generated; audit columns
	 * ({@code created_at}/{@code updated_at}) use their DB defaults. Status starts ACTIVE.
	 */
	public User(UUID id, UUID organizationId, String email, String passwordHash, String fullName,
			String avatarInitials, String avatarColor) {
		this.id = id;
		this.organizationId = organizationId;
		this.email = email;
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.avatarInitials = avatarInitials;
		this.avatarColor = avatarColor;
		this.status = "ACTIVE";
	}

	/** Update editable profile fields (administrator edit). Null arguments leave a field unchanged. */
	public void updateProfile(String fullName, String avatarInitials, String avatarColor) {
		if (fullName != null) {
			this.fullName = fullName;
		}
		if (avatarInitials != null) {
			this.avatarInitials = avatarInitials;
		}
		if (avatarColor != null) {
			this.avatarColor = avatarColor;
		}
	}

	/** Set the account status (ACTIVE | INACTIVE | SUSPENDED). */
	public void changeStatus(String status) {
		this.status = status;
	}

	public void addRole(Role role) {
		this.roles.add(role);
	}

	public void removeRole(Role role) {
		this.roles.remove(role);
	}

	public UUID getId() {
		return id;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getFullName() {
		return fullName;
	}

	public String getAvatarInitials() {
		return avatarInitials;
	}

	public String getAvatarColor() {
		return avatarColor;
	}

	public String getStatus() {
		return status;
	}

	public Instant getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(Instant lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public boolean isActive() {
		return "ACTIVE".equals(status);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof User other)) {
			return false;
		}
		return id != null && id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
