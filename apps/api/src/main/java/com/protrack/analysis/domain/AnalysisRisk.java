package com.protrack.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/** A flagged production risk with severity (HIGH/MEDIUM/LOW). */
@Entity
@Table(name = "analysis_risks")
public class AnalysisRisk {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "analysis_result_id", nullable = false)
	private AnalysisResult analysisResult;

	@Column(nullable = false)
	private String severity;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	protected AnalysisRisk() {
	}

	public AnalysisRisk(UUID id, AnalysisResult analysisResult, String severity, String title,
			String description) {
		this.id = id;
		this.analysisResult = analysisResult;
		this.severity = severity;
		this.title = title;
		this.description = description;
	}

	public UUID getId() {
		return id;
	}

	public String getSeverity() {
		return severity;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}
}
