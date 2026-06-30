package com.protrack.workflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A pipeline stage (reference data seeded in V4). */
@Entity
@Table(name = "workflow_stages")
public class WorkflowStage {

	@Id
	private Integer id;

	@Column(nullable = false)
	private String code;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer sequence;

	private String description;

	protected WorkflowStage() {
	}

	public Integer getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public Integer getSequence() {
		return sequence;
	}

	public String getDescription() {
		return description;
	}
}
