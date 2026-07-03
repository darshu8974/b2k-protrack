package com.protrack.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/** A heading-level count (H1/H2/H3). */
@Entity
@Table(name = "analysis_headings")
public class AnalysisHeading {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "analysis_result_id", nullable = false)
	private AnalysisResult analysisResult;

	@Column(nullable = false)
	private String level;

	@Column(nullable = false)
	private int count;

	protected AnalysisHeading() {
	}

	public AnalysisHeading(UUID id, AnalysisResult analysisResult, String level, int count) {
		this.id = id;
		this.analysisResult = analysisResult;
		this.level = level;
		this.count = count;
	}

	public UUID getId() {
		return id;
	}

	public String getLevel() {
		return level;
	}

	public int getCount() {
		return count;
	}
}
