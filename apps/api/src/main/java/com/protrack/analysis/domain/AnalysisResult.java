package com.protrack.analysis.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The header of a manuscript analysis (one per successful {@code MANUSCRIPT_ANALYSIS} job). Owns the
 * normalized child rows (metrics/composition/headings/risks/team) and keeps the full model output in
 * {@code rawPayload} (JSONB) for provenance. {@code createdAt} is set by the database default.
 */
@Entity
@Table(name = "analysis_results")
public class AnalysisResult {

	@Id
	private UUID id;

	@Column(name = "ai_job_id", nullable = false)
	private UUID aiJobId;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "overall_confidence")
	private BigDecimal overallConfidence;

	@Column(columnDefinition = "text")
	private String summary;

	private String language;

	@Column(name = "complexity_score")
	private Integer complexityScore;

	@Column(name = "complexity_label")
	private String complexityLabel;

	@Column(name = "estimated_working_days")
	private Integer estimatedWorkingDays;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "raw_payload", columnDefinition = "jsonb")
	private String rawPayload;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AnalysisMetric> metrics = new ArrayList<>();

	@OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AnalysisComposition> composition = new ArrayList<>();

	@OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AnalysisHeading> headings = new ArrayList<>();

	@OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AnalysisRisk> risks = new ArrayList<>();

	@OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TeamSuggestion> teamSuggestions = new ArrayList<>();

	protected AnalysisResult() {
	}

	public AnalysisResult(UUID id, UUID aiJobId, UUID projectId, BigDecimal overallConfidence,
			String summary, String language, Integer complexityScore, String complexityLabel,
			Integer estimatedWorkingDays, String rawPayload) {
		this.id = id;
		this.aiJobId = aiJobId;
		this.projectId = projectId;
		this.overallConfidence = overallConfidence;
		this.summary = summary;
		this.language = language;
		this.complexityScore = complexityScore;
		this.complexityLabel = complexityLabel;
		this.estimatedWorkingDays = estimatedWorkingDays;
		this.rawPayload = rawPayload;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAiJobId() {
		return aiJobId;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public BigDecimal getOverallConfidence() {
		return overallConfidence;
	}

	public String getSummary() {
		return summary;
	}

	public String getLanguage() {
		return language;
	}

	public Integer getComplexityScore() {
		return complexityScore;
	}

	public String getComplexityLabel() {
		return complexityLabel;
	}

	public Integer getEstimatedWorkingDays() {
		return estimatedWorkingDays;
	}

	public String getRawPayload() {
		return rawPayload;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public List<AnalysisMetric> getMetrics() {
		return metrics;
	}

	public List<AnalysisComposition> getComposition() {
		return composition;
	}

	public List<AnalysisHeading> getHeadings() {
		return headings;
	}

	public List<AnalysisRisk> getRisks() {
		return risks;
	}

	public List<TeamSuggestion> getTeamSuggestions() {
		return teamSuggestions;
	}
}
