package com.protrack.analysis.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.protrack.ai.client.dto.AnalysisResponse;
import com.protrack.ai.client.dto.AnalysisResponse.CompositionDto;
import com.protrack.ai.client.dto.AnalysisResponse.HeadingDto;
import com.protrack.ai.client.dto.AnalysisResponse.MetricDto;
import com.protrack.ai.client.dto.AnalysisResponse.RiskDto;
import com.protrack.ai.client.dto.AnalysisResponse.TeamDto;
import com.protrack.ai.client.dto.AnalysisResponse.UsageDto;
import com.protrack.analysis.domain.AnalysisResult;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AnalysisMapper} (no Spring context, no Docker). */
class AnalysisMapperTest {

	private final AnalysisMapper mapper = new AnalysisMapper();

	private static AnalysisResponse sampleResponse() {
		return new AnalysisResponse(
				84, "Solid manuscript.", "en", 62, "Moderate", 18,
				List.of(new MetricDto("pages", 120L, 100), new MetricDto("figures", 8L, 100)),
				List.of(new CompositionDto("Body text", 68.0)),
				List.of(new HeadingDto("H1", 2), new HeadingDto("H2", 1)),
				List.of(new RiskDto("MEDIUM", "Notation", "Mixed notation.")),
				List.of(new TeamDto("Copyeditor", 88, "STEM experience.", null)),
				"manuscript_analysis.v1", "mock", new UsageDto(0, 0, "mock"));
	}

	@Test
	void mapsHeaderAndChildrenWithScaleConversions() {
		UUID jobId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();

		AnalysisResult result = mapper.toEntity(jobId, projectId, sampleResponse(), "{\"raw\":true}");

		assertThat(result.getAiJobId()).isEqualTo(jobId);
		assertThat(result.getProjectId()).isEqualTo(projectId);
		assertThat(result.getOverallConfidence()).isEqualByComparingTo("84");
		assertThat(result.getComplexityScore()).isEqualTo(62);
		assertThat(result.getComplexityLabel()).isEqualTo("Moderate");
		assertThat(result.getEstimatedWorkingDays()).isEqualTo(18);
		assertThat(result.getRawPayload()).isEqualTo("{\"raw\":true}");

		assertThat(result.getMetrics()).hasSize(2);
		assertThat(result.getMetrics().get(0).getMetricKey()).isEqualTo("pages");
		assertThat(result.getMetrics().get(0).getMetricValue()).isEqualTo(120L);
		assertThat(result.getMetrics().get(0).getConfidence()).isEqualByComparingTo("100");

		assertThat(result.getComposition()).hasSize(1);
		assertThat(result.getComposition().get(0).getPercentage()).isEqualByComparingTo("68.0");

		assertThat(result.getHeadings()).extracting("level").containsExactly("H1", "H2");
		assertThat(result.getRisks().get(0).getSeverity()).isEqualTo("MEDIUM");

		assertThat(result.getTeamSuggestions()).hasSize(1);
		assertThat(result.getTeamSuggestions().get(0).getSuggestedRole()).isEqualTo("Copyeditor");
		assertThat(result.getTeamSuggestions().get(0).getMatchScore()).isEqualByComparingTo("88");
		// Phase 1: the model returns a role/hint, not a resolved user.
		assertThat(result.getTeamSuggestions().get(0).getSuggestedUserId()).isNull();
	}

	@Test
	void toleratesNullCollectionsAndValues() {
		AnalysisResponse sparse = new AnalysisResponse(
				null, null, null, null, null, null, null, null, null, null, null, null, null, null);

		AnalysisResult result = mapper.toEntity(UUID.randomUUID(), UUID.randomUUID(), sparse, null);

		assertThat(result.getOverallConfidence()).isNull();
		assertThat(result.getMetrics()).isEmpty();
		assertThat(result.getComposition()).isEmpty();
		assertThat(result.getHeadings()).isEmpty();
		assertThat(result.getRisks()).isEmpty();
		assertThat(result.getTeamSuggestions()).isEmpty();
	}
}
