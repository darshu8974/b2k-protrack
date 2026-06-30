package com.protrack.project.service;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.project.domain.Project;
import com.protrack.project.mapper.ProjectMapper;
import com.protrack.project.repository.ProjectRepository;
import com.protrack.project.repository.StageCountView;
import com.protrack.project.repository.StatusCountView;
import com.protrack.project.web.dto.DashboardResponse;
import com.protrack.project.web.dto.DashboardResponse.Kpis;
import com.protrack.project.web.dto.DashboardResponse.StageCount;
import com.protrack.project.web.dto.DashboardResponse.StatusCount;
import com.protrack.project.web.dto.ProjectSummaryResponse;
import com.protrack.shared.error.ApiException;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregates the dashboard summary using single grouped queries (no per-project hydration). */
@Service
public class DashboardService {

	/** Canonical pipeline order, so stage counts are always returned in sequence. */
	private static final List<String> STAGE_ORDER = List.of(
			"INTAKE", "AI_ANALYSIS", "DESIGN_PREP", "IN_PRODUCTION", "PDF_REVIEW", "QA_SIGNOFF", "COMPLETED");
	private static final int MY_PROJECTS_LIMIT = 12;

	private final ProjectRepository projectRepository;
	private final IdentityFacade identityFacade;
	private final ProjectMapper mapper;

	public DashboardService(ProjectRepository projectRepository, IdentityFacade identityFacade,
			ProjectMapper mapper) {
		this.projectRepository = projectRepository;
		this.identityFacade = identityFacade;
		this.mapper = mapper;
	}

	@Transactional(readOnly = true)
	public DashboardResponse getDashboard(UUID currentUserId) {
		UUID organizationId = identityFacade.findOrganizationId(currentUserId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
						"Authenticated user no longer exists."));

		Map<String, Long> stageMap = projectRepository.countByStage(organizationId).stream()
				.collect(Collectors.toMap(StageCountView::getStage, StageCountView::getTotal));
		Map<String, Long> statusMap = projectRepository.countByStatus(organizationId).stream()
				.collect(Collectors.toMap(StatusCountView::getStatus, StatusCountView::getTotal));

		Instant monthStart = YearMonth.now(ZoneOffset.UTC).atDay(1)
				.atStartOfDay(ZoneOffset.UTC).toInstant();
		long completedThisMonth = projectRepository.countCompletedSince(organizationId, monthStart);
		long total = statusMap.values().stream().mapToLong(Long::longValue).sum();

		Kpis kpis = new Kpis(
				statusMap.getOrDefault("ACTIVE", 0L),
				stageMap.getOrDefault("IN_PRODUCTION", 0L),
				stageMap.getOrDefault("QA_SIGNOFF", 0L),
				completedThisMonth,
				total);

		List<StageCount> stageCounts = STAGE_ORDER.stream()
				.map(code -> new StageCount(code, stageMap.getOrDefault(code, 0L)))
				.toList();
		List<StatusCount> statusCounts = statusMap.entrySet().stream()
				.map(entry -> new StatusCount(entry.getKey(), entry.getValue()))
				.toList();

		List<Project> recent = projectRepository
				.findTop5ByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(organizationId);
		List<Project> mine = projectRepository.findAssignedTo(organizationId, currentUserId).stream()
				.limit(MY_PROJECTS_LIMIT).toList();

		Set<UUID> ownerIds = Stream.concat(recent.stream(), mine.stream())
				.map(Project::getOwnerId).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<UUID, UserBrief> owners = identityFacade.findBriefs(ownerIds);

		return new DashboardResponse(kpis, stageCounts, statusCounts,
				toSummaries(recent, owners), toSummaries(mine, owners));
	}

	private List<ProjectSummaryResponse> toSummaries(List<Project> projects, Map<UUID, UserBrief> owners) {
		return projects.stream()
				.map(project -> mapper.toSummary(project, ownerName(owners, project.getOwnerId())))
				.toList();
	}

	private static String ownerName(Map<UUID, UserBrief> owners, UUID ownerId) {
		if (ownerId == null) {
			return null;
		}
		UserBrief brief = owners.get(ownerId);
		return brief == null ? null : brief.fullName();
	}
}
