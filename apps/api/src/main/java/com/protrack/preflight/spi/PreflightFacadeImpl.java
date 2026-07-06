package com.protrack.preflight.spi;

import com.protrack.preflight.domain.QaIssue;
import com.protrack.preflight.repository.PreflightRunRepository;
import com.protrack.preflight.repository.QaIssueRepository;
import com.protrack.shared.error.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link PreflightFacade} backed by the preflight repositories. */
@Service
public class PreflightFacadeImpl implements PreflightFacade {

	private final QaIssueRepository issueRepository;
	private final PreflightRunRepository runRepository;

	public PreflightFacadeImpl(QaIssueRepository issueRepository,
			PreflightRunRepository runRepository) {
		this.issueRepository = issueRepository;
		this.runRepository = runRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<IssueRef> findIssues(UUID projectId, String severity, String status) {
		return issueRepository.findFiltered(projectId, blankToNull(severity), blankToNull(status))
				.stream().map(PreflightFacadeImpl::toRef).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<IssueRef> findIssue(UUID issueId) {
		return issueRepository.findById(issueId).map(PreflightFacadeImpl::toRef);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IssueRef> findIssuesBySeverity(UUID projectId, String severity) {
		return issueRepository.findByProjectIdAndSeverity(projectId, severity)
				.stream().map(PreflightFacadeImpl::toRef).toList();
	}

	@Override
	@Transactional
	public void applyIssueStatus(UUID issueId, String status) {
		QaIssue issue = issueRepository.findById(issueId)
				.orElseThrow(() -> new NotFoundException("Issue not found."));
		issue.applyStatus(status);
		issueRepository.save(issue);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<RunRef> findLatestRun(UUID projectId) {
		return runRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
				.map(run -> new RunRef(run.getId(), run.getProjectId(), run.getOverallScore(),
						run.getPassed()));
	}

	private static IssueRef toRef(QaIssue issue) {
		return new IssueRef(issue.getId(), issue.getProjectId(), issue.getPreflightRunId(),
				issue.getCategory(), issue.getSeverity(), issue.getTitle(), issue.getRecommendation(),
				issue.getPageRef(), issue.getSource(), issue.getStatus(), issue.getCreatedAt());
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}
}
