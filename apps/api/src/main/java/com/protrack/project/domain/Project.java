package com.protrack.project.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The central project aggregate. Cross-module references (organization, owner) are held as UUIDs;
 * the intra-module imprint is a JPA association. Timestamps are maintained by lifecycle callbacks;
 * {@code createdAt} is populated by the database default.
 */
@Entity
@Table(name = "projects")
public class Project {

	@Id
	private UUID id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "imprint_id")
	private Imprint imprint;

	@Column(name = "owner_id")
	private UUID ownerId;

	@Column(nullable = false)
	private String title;

	private String isbn;

	@Column(name = "publication_type", nullable = false)
	private String publicationType;

	private String discipline;

	@Column(columnDefinition = "text")
	private String brief;

	@Column(name = "page_extent")
	private Integer pageExtent;

	@Column(name = "trim_size")
	private String trimSize;

	@Column(nullable = false)
	private String priority;

	@Column(name = "current_stage", nullable = false)
	private String currentStage;

	@Column(nullable = false)
	private String status;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Column(name = "created_date", nullable = false)
	private LocalDate createdDate;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "created_by")
	private UUID createdBy;

	@Column(name = "updated_by")
	private UUID updatedBy;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProjectMember> members = new ArrayList<>();

	public Project() {
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		if (updatedAt == null) {
			updatedAt = now;
		}
		if (createdDate == null) {
			createdDate = LocalDate.now();
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	// --- getters / setters ---

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(UUID organizationId) {
		this.organizationId = organizationId;
	}

	public Imprint getImprint() {
		return imprint;
	}

	public void setImprint(Imprint imprint) {
		this.imprint = imprint;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getPublicationType() {
		return publicationType;
	}

	public void setPublicationType(String publicationType) {
		this.publicationType = publicationType;
	}

	public String getDiscipline() {
		return discipline;
	}

	public void setDiscipline(String discipline) {
		this.discipline = discipline;
	}

	public String getBrief() {
		return brief;
	}

	public void setBrief(String brief) {
		this.brief = brief;
	}

	public Integer getPageExtent() {
		return pageExtent;
	}

	public void setPageExtent(Integer pageExtent) {
		this.pageExtent = pageExtent;
	}

	public String getTrimSize() {
		return trimSize;
	}

	public void setTrimSize(String trimSize) {
		this.trimSize = trimSize;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getCurrentStage() {
		return currentStage;
	}

	public void setCurrentStage(String currentStage) {
		this.currentStage = currentStage;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setCreatedBy(UUID createdBy) {
		this.createdBy = createdBy;
	}

	public void setUpdatedBy(UUID updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public List<ProjectMember> getMembers() {
		return members;
	}
}
