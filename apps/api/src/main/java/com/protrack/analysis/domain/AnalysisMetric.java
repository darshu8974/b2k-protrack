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

/** A single analysis metric card (pages, figures, equations, …) with a confidence. */
@Entity
@Table(name = "analysis_metrics")
public class AnalysisMetric {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "analysis_result_id", nullable = false)
	private AnalysisResult analysisResult;

	@Column(name = "metric_key", nullable = false)
	private String metricKey;

	@Column(name = "metric_value")
	private Long metricValue;

	private BigDecimal confidence;

	protected AnalysisMetric() {
	}

	public AnalysisMetric(UUID id, AnalysisResult analysisResult, String metricKey, Long metricValue,
			BigDecimal confidence) {
		this.id = id;
		this.analysisResult = analysisResult;
		this.metricKey = metricKey;
		this.metricValue = metricValue;
		this.confidence = confidence;
	}

	public UUID getId() {
		return id;
	}

	public String getMetricKey() {
		return metricKey;
	}

	public Long getMetricValue() {
		return metricValue;
	}

	public BigDecimal getConfidence() {
		return confidence;
	}
}
