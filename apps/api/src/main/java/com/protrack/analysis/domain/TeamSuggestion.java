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

/**
 * An AI-matched team suggestion (role + match score + rationale). {@code suggestedUserId} is left
 * null in Phase 1 — the model returns a role/candidate hint, not a resolved user; converting a
 * suggestion into an actual {@code project_member} is a later, human-triggered action.
 */
@Entity
@Table(name = "team_suggestions")
public class TeamSuggestion {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "analysis_result_id", nullable = false)
	private AnalysisResult analysisResult;

	@Column(name = "suggested_user_id")
	private UUID suggestedUserId;

	@Column(name = "suggested_role")
	private String suggestedRole;

	@Column(name = "match_score")
	private BigDecimal matchScore;

	@Column(columnDefinition = "text")
	private String rationale;

	protected TeamSuggestion() {
	}

	public TeamSuggestion(UUID id, AnalysisResult analysisResult, UUID suggestedUserId,
			String suggestedRole, BigDecimal matchScore, String rationale) {
		this.id = id;
		this.analysisResult = analysisResult;
		this.suggestedUserId = suggestedUserId;
		this.suggestedRole = suggestedRole;
		this.matchScore = matchScore;
		this.rationale = rationale;
	}

	public UUID getId() {
		return id;
	}

	public UUID getSuggestedUserId() {
		return suggestedUserId;
	}

	public String getSuggestedRole() {
		return suggestedRole;
	}

	public BigDecimal getMatchScore() {
		return matchScore;
	}

	public String getRationale() {
		return rationale;
	}
}
