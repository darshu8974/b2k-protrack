package com.protrack.preflight.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.protrack.ai.client.dto.PreflightResponse;
import com.protrack.ai.client.dto.PreflightResponse.CheckDto;
import com.protrack.ai.client.dto.PreflightResponse.IssueDto;
import com.protrack.ai.client.dto.PreflightResponse.TotalsDto;
import com.protrack.ai.client.dto.PreflightResponse.UsageDto;
import com.protrack.preflight.domain.QaIssue;
import com.protrack.preflight.mapper.PreflightMapper.Mapped;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PreflightMapper} (no Spring context, no Docker). */
class PreflightMapperTest {

	private final PreflightMapper mapper = new PreflightMapper();

	private static PreflightResponse sampleResponse() {
		return new PreflightResponse(
				82, true, "PDF/X-4",
				List.of(
						new CheckDto("geometry", "PASS", "Uniform size."),
						new CheckDto("font_embedding", "FAIL", "Arial not embedded."),
						new CheckDto("accessibility", "REVIEW", "Untagged PDF.")),
				List.of(
						new IssueDto("fonts", "HIGH", "Fonts not embedded", "Embed all fonts.",
								"pages 2-5", "AI"),
						new IssueDto("accessibility", "LOW", "Untagged PDF", "Export tagged PDF.",
								null, "AI")),
				new TotalsDto(2, 1), "preflight_findings.v1", "mock", new UsageDto(0, 0, "mock"));
	}

	@Test
	void mapsRunHeaderChecksAndIssues() {
		UUID jobId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		UUID versionId = UUID.randomUUID();

		Mapped mapped = mapper.toEntities(jobId, projectId, versionId, sampleResponse());

		assertThat(mapped.run().getAiJobId()).isEqualTo(jobId);
		assertThat(mapped.run().getProjectId()).isEqualTo(projectId);
		assertThat(mapped.run().getPdfVersionId()).isEqualTo(versionId);
		assertThat(mapped.run().getStandard()).isEqualTo("PDF/X-4");
		assertThat(mapped.run().getOverallScore()).isEqualTo(82);
		assertThat(mapped.run().getPassed()).isTrue();
		assertThat(mapped.run().getStatus()).isEqualTo("SUCCEEDED");
		assertThat(mapped.run().getTotalIssues()).isEqualTo(2);
		assertThat(mapped.run().getHighSeverity()).isEqualTo(1);

		// Checks keep their order via sort_order.
		assertThat(mapped.run().getChecks()).hasSize(3);
		assertThat(mapped.run().getChecks()).extracting("checkKey")
				.containsExactly("geometry", "font_embedding", "accessibility");
		assertThat(mapped.run().getChecks()).extracting("sortOrder").containsExactly(0, 1, 2);
		assertThat(mapped.run().getChecks().get(1).getResult()).isEqualTo("FAIL");

		// Issues are created OPEN with source AI, carrying the run + project ids.
		assertThat(mapped.issues()).hasSize(2);
		QaIssue high = mapped.issues().get(0);
		assertThat(high.getSeverity()).isEqualTo("HIGH");
		assertThat(high.getTitle()).isEqualTo("Fonts not embedded");
		assertThat(high.getPageRef()).isEqualTo("pages 2-5");
		assertThat(high.getSource()).isEqualTo("AI");
		assertThat(high.getStatus()).isEqualTo("OPEN");
		assertThat(high.getProjectId()).isEqualTo(projectId);
		assertThat(high.getPreflightRunId()).isEqualTo(mapped.run().getId());
	}

	@Test
	void derivesTotalsWhenAbsentAndToleratesNulls() {
		PreflightResponse noTotals = new PreflightResponse(
				90, true, null,
				null,
				List.of(new IssueDto(null, "HIGH", "A", null, null, null),
						new IssueDto(null, "LOW", "B", null, null, null)),
				null, null, null, null);

		Mapped mapped = mapper.toEntities(
				UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), noTotals);

		// totals absent → computed from the issue list (2 total, 1 high).
		assertThat(mapped.run().getTotalIssues()).isEqualTo(2);
		assertThat(mapped.run().getHighSeverity()).isEqualTo(1);
		assertThat(mapped.run().getChecks()).isEmpty();
		// source defaults to AI when the response omits it.
		assertThat(mapped.issues().get(0).getSource()).isEqualTo("AI");
	}
}
