package com.protrack.preflight.mapper;

import com.protrack.ai.client.dto.PreflightResponse;
import com.protrack.preflight.domain.IssueStatus;
import com.protrack.preflight.domain.PreflightCheck;
import com.protrack.preflight.domain.PreflightRun;
import com.protrack.preflight.domain.QaIssue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Maps the AI service's normalized {@link PreflightResponse} into the {@code preflight_runs} /
 * {@code preflight_checks} / {@code qa_issues} rows. The mapping is mechanical (the response already
 * conforms to the DB conventions); {@code sort_order} is assigned from the checks' array order and
 * issues are created OPEN with {@code source = AI}.
 */
@Component
public class PreflightMapper {

	private static final String SUCCEEDED = "SUCCEEDED";
	private static final String SOURCE_AI = "AI";

	/** The persisted run (with its checks) and the standalone issues it raised. */
	public record Mapped(PreflightRun run, List<QaIssue> issues) {
	}

	public Mapped toEntities(UUID aiJobId, UUID projectId, UUID pdfVersionId,
			PreflightResponse response) {
		int totalIssues = response.totals() == null || response.totals().issues() == null
				? nullSafe(response.issues()).size() : response.totals().issues();
		int highSeverity = response.totals() == null || response.totals().high() == null
				? countHigh(response) : response.totals().high();

		PreflightRun run = new PreflightRun(
				UUID.randomUUID(), aiJobId, projectId, pdfVersionId, response.standard(),
				response.overallScore(), response.passed(), totalIssues, highSeverity,
				SUCCEEDED, Instant.now());

		int order = 0;
		for (PreflightResponse.CheckDto check : nullSafe(response.checks())) {
			run.getChecks().add(new PreflightCheck(
					UUID.randomUUID(), run, check.key(), check.result(), check.detail(), order++));
		}

		List<QaIssue> issues = new ArrayList<>();
		for (PreflightResponse.IssueDto issue : nullSafe(response.issues())) {
			issues.add(new QaIssue(
					UUID.randomUUID(), run.getId(), projectId, issue.category(), issue.severity(),
					issue.title(), issue.recommendation(), issue.pageRef(),
					issue.source() == null ? SOURCE_AI : issue.source(), IssueStatus.OPEN.name()));
		}
		return new Mapped(run, issues);
	}

	private static int countHigh(PreflightResponse response) {
		return (int) nullSafe(response.issues()).stream()
				.filter(issue -> "HIGH".equals(issue.severity())).count();
	}

	private static <T> List<T> nullSafe(List<T> list) {
		return list == null ? List.of() : list;
	}
}
