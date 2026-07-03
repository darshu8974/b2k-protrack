package com.protrack.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

/** A document-composition donut segment (body / equations / figures / …) with its percentage. */
@Entity
@Table(name = "analysis_composition")
public class AnalysisComposition {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "analysis_result_id", nullable = false)
	private AnalysisResult analysisResult;

	@Column(nullable = false)
	private String segment;

	@Column(nullable = false)
	private BigDecimal percentage;

	protected AnalysisComposition() {
	}

	public AnalysisComposition(UUID id, AnalysisResult analysisResult, String segment,
			BigDecimal percentage) {
		this.id = id;
		this.analysisResult = analysisResult;
		this.segment = segment;
		this.percentage = percentage;
	}

	public UUID getId() {
		return id;
	}

	public String getSegment() {
		return segment;
	}

	public BigDecimal getPercentage() {
		return percentage;
	}
}
