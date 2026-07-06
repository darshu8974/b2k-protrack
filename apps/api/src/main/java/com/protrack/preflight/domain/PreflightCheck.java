package com.protrack.preflight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/** A single preflight checklist item (geometry, font embedding, …) with its PASS/REVIEW/FAIL result. */
@Entity
@Table(name = "preflight_checks")
public class PreflightCheck {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "preflight_run_id", nullable = false)
	private PreflightRun preflightRun;

	@Column(name = "check_key", nullable = false)
	private String checkKey;

	@Column(nullable = false)
	private String result;

	@Column(columnDefinition = "text")
	private String detail;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	protected PreflightCheck() {
	}

	public PreflightCheck(UUID id, PreflightRun preflightRun, String checkKey, String result,
			String detail, int sortOrder) {
		this.id = id;
		this.preflightRun = preflightRun;
		this.checkKey = checkKey;
		this.result = result;
		this.detail = detail;
		this.sortOrder = sortOrder;
	}

	public UUID getId() {
		return id;
	}

	public String getCheckKey() {
		return checkKey;
	}

	public String getResult() {
		return result;
	}

	public String getDetail() {
		return detail;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
