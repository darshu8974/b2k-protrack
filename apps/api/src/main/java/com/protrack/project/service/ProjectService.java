package com.protrack.project.service;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.project.domain.Imprint;
import com.protrack.project.domain.Priority;
import com.protrack.project.domain.Project;
import com.protrack.project.domain.ProjectMember;
import com.protrack.project.domain.ProjectStatus;
import com.protrack.project.mapper.ProjectMapper;
import com.protrack.project.repository.ImprintRepository;
import com.protrack.project.repository.ProjectRepository;
import com.protrack.project.repository.ProjectSpecifications;
import com.protrack.project.web.dto.AssignMembersRequest;
import com.protrack.project.web.dto.CreateProjectRequest;
import com.protrack.project.web.dto.ProjectMemberResponse;
import com.protrack.project.web.dto.ProjectResponse;
import com.protrack.project.web.dto.ProjectSummaryResponse;
import com.protrack.project.web.dto.UpdateProjectRequest;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.ConflictException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.events.ProjectEvents;
import com.protrack.shared.web.PageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Project CRUD, member assignment, and list filtering. No workflow transitions (later task). */
@Service
public class ProjectService {

	private static final String OWNER_ROLE = "PROJECT_MANAGER";

	private final ProjectRepository projectRepository;
	private final ImprintRepository imprintRepository;
	private final IdentityFacade identityFacade;
	private final ProjectMapper mapper;
	private final ApplicationEventPublisher eventPublisher;

	public ProjectService(ProjectRepository projectRepository, ImprintRepository imprintRepository,
			IdentityFacade identityFacade, ProjectMapper mapper,
			ApplicationEventPublisher eventPublisher) {
		this.projectRepository = projectRepository;
		this.imprintRepository = imprintRepository;
		this.identityFacade = identityFacade;
		this.mapper = mapper;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public ProjectResponse create(UUID currentUserId, CreateProjectRequest request) {
		UUID organizationId = organizationOf(currentUserId);
		Imprint imprint = requireImprint(request.imprintId(), organizationId);
		String isbn = validateIsbn(request.isbn(), null);

		Project project = new Project();
		project.setId(UUID.randomUUID());
		project.setOrganizationId(organizationId);
		project.setImprint(imprint);
		project.setOwnerId(currentUserId);
		project.setTitle(request.title().trim());
		project.setIsbn(isbn);
		project.setPublicationType(request.publicationType().name());
		project.setDiscipline(request.discipline());
		project.setBrief(request.brief());
		project.setPageExtent(request.pageExtent());
		project.setTrimSize(request.trimSize());
		project.setPriority((request.priority() == null ? Priority.MEDIUM : request.priority()).name());
		project.setCurrentStage("INTAKE");
		project.setStatus(ProjectStatus.ACTIVE.name());
		project.setDueDate(request.dueDate());
		project.setCreatedBy(currentUserId);
		project.setUpdatedBy(currentUserId);

		// Owner is always a member.
		project.getMembers().add(new ProjectMember(
				UUID.randomUUID(), project, currentUserId, OWNER_ROLE, true, null, currentUserId));

		// Additional members from the request (validated, deduped against the owner).
		List<UUID> extraMembers = request.memberUserIds() == null ? List.of()
				: request.memberUserIds().stream().filter(id -> !id.equals(currentUserId)).distinct().toList();
		if (!extraMembers.isEmpty()) {
			requireUsersExist(extraMembers);
			extraMembers.forEach(userId -> project.getMembers().add(new ProjectMember(
					UUID.randomUUID(), project, userId, null, false, null, currentUserId)));
		}

		Project saved = projectRepository.save(project);
		eventPublisher.publishEvent(new ProjectEvents.ProjectCreated(
				organizationId, saved.getId(), currentUserId, saved.getTitle()));
		return buildResponse(saved);
	}

	@Transactional(readOnly = true)
	public ProjectResponse get(UUID id) {
		Project project = projectRepository.findWithDetailsByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new NotFoundException("Project not found."));
		return buildResponse(project);
	}

	@Transactional
	public ProjectResponse update(UUID currentUserId, UUID id, UpdateProjectRequest request) {
		UUID organizationId = organizationOf(currentUserId);
		Project project = projectRepository.findWithDetailsByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new NotFoundException("Project not found."));

		if (request.title() != null) {
			project.setTitle(request.title().trim());
		}
		if (request.isbn() != null) {
			project.setIsbn(validateIsbn(request.isbn(), id));
		}
		if (request.imprintId() != null) {
			project.setImprint(requireImprint(request.imprintId(), organizationId));
		}
		if (request.publicationType() != null) {
			project.setPublicationType(request.publicationType().name());
		}
		if (request.discipline() != null) {
			project.setDiscipline(request.discipline());
		}
		if (request.brief() != null) {
			project.setBrief(request.brief());
		}
		if (request.pageExtent() != null) {
			project.setPageExtent(request.pageExtent());
		}
		if (request.trimSize() != null) {
			project.setTrimSize(request.trimSize());
		}
		if (request.priority() != null) {
			project.setPriority(request.priority().name());
		}
		if (request.status() != null) {
			if (request.status() == ProjectStatus.COMPLETED) {
				throw new ConflictException("INVALID_STATUS",
						"Completion happens through the workflow, not a direct update.");
			}
			project.setStatus(request.status().name());
		}
		if (request.dueDate() != null) {
			project.setDueDate(request.dueDate());
		}
		project.setUpdatedBy(currentUserId);

		Project saved = projectRepository.save(project);
		eventPublisher.publishEvent(new ProjectEvents.ProjectUpdated(
				saved.getOrganizationId(), saved.getId(), currentUserId));
		return buildResponse(saved);
	}

	@Transactional(readOnly = true)
	public PageResponse<ProjectSummaryResponse> list(UUID currentUserId, String stage, UUID imprintId,
			String status, String priority, boolean mine, String query, Pageable pageable) {
		UUID organizationId = organizationOf(currentUserId);

		Specification<Project> spec = Specification
				.where(ProjectSpecifications.inOrganization(organizationId))
				.and(ProjectSpecifications.notDeleted());
		if (StringUtils.hasText(stage)) {
			spec = spec.and(ProjectSpecifications.hasStage(stage));
		}
		if (imprintId != null) {
			spec = spec.and(ProjectSpecifications.hasImprint(imprintId));
		}
		if (StringUtils.hasText(status)) {
			spec = spec.and(ProjectSpecifications.hasStatus(status));
		}
		if (StringUtils.hasText(priority)) {
			spec = spec.and(ProjectSpecifications.hasPriority(priority));
		}
		if (mine) {
			spec = spec.and(ProjectSpecifications.ownedBy(currentUserId));
		}
		if (StringUtils.hasText(query)) {
			spec = spec.and(ProjectSpecifications.search(query));
		}

		Page<Project> page = projectRepository.findAll(spec, pageable);
		Set<UUID> ownerIds = page.getContent().stream()
				.map(Project::getOwnerId).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<UUID, UserBrief> ownerBriefs = identityFacade.findBriefs(ownerIds);

		Page<ProjectSummaryResponse> mapped = page.map(project -> {
			UserBrief owner = project.getOwnerId() == null ? null : ownerBriefs.get(project.getOwnerId());
			return mapper.toSummary(project, owner == null ? null : owner.fullName());
		});
		return PageResponse.of(mapped);
	}

	@Transactional
	public List<ProjectMemberResponse> assignMembers(UUID currentUserId, UUID projectId,
			AssignMembersRequest request) {
		Project project = projectRepository.findWithDetailsByIdAndDeletedAtIsNull(projectId)
				.orElseThrow(() -> new NotFoundException("Project not found."));

		List<UUID> userIds = request.members().stream()
				.map(AssignMembersRequest.MemberAssignment::userId).distinct().toList();
		requireUsersExist(userIds);

		for (AssignMembersRequest.MemberAssignment assignment : request.members()) {
			Optional<ProjectMember> existing = project.getMembers().stream()
					.filter(member -> member.getUserId().equals(assignment.userId())).findFirst();
			if (existing.isPresent()) {
				existing.get().setRoleInProject(assignment.roleInProject());
				existing.get().setMatchScore(assignment.matchScore());
			} else {
				project.getMembers().add(new ProjectMember(UUID.randomUUID(), project, assignment.userId(),
						assignment.roleInProject(), false, assignment.matchScore(), currentUserId));
			}
		}
		project.setUpdatedBy(currentUserId);
		Project saved = projectRepository.save(project);
		eventPublisher.publishEvent(new ProjectEvents.ProjectMembersAssigned(
				saved.getOrganizationId(), saved.getId(), currentUserId, saved.getMembers().size()));

		Map<UUID, UserBrief> briefs = identityFacade.findBriefs(
				saved.getMembers().stream().map(ProjectMember::getUserId).toList());
		return saved.getMembers().stream()
				.map(member -> mapper.toMemberResponse(member, briefs.get(member.getUserId())))
				.toList();
	}

	// --- helpers ---

	private ProjectResponse buildResponse(Project project) {
		List<UUID> ids = new ArrayList<>(project.getMembers().stream()
				.map(ProjectMember::getUserId).toList());
		if (project.getOwnerId() != null) {
			ids.add(project.getOwnerId());
		}
		Map<UUID, UserBrief> briefs = identityFacade.findBriefs(ids);
		UserBrief owner = project.getOwnerId() == null ? null : briefs.get(project.getOwnerId());
		return mapper.toResponse(project, owner, briefs);
	}

	private UUID organizationOf(UUID userId) {
		return identityFacade.findOrganizationId(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
						"Authenticated user no longer exists."));
	}

	private Imprint requireImprint(UUID imprintId, UUID organizationId) {
		Imprint imprint = imprintRepository.findById(imprintId)
				.orElseThrow(() -> new NotFoundException("Imprint not found."));
		if (!imprint.getOrganizationId().equals(organizationId)) {
			throw new NotFoundException("Imprint not found.");
		}
		return imprint;
	}

	private void requireUsersExist(List<UUID> userIds) {
		Set<UUID> existing = identityFacade.findExistingIds(userIds);
		if (!existing.containsAll(userIds)) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_MEMBERS",
					"One or more member users do not exist.");
		}
	}

	private String validateIsbn(String rawIsbn, UUID excludeProjectId) {
		if (!StringUtils.hasText(rawIsbn)) {
			return null;
		}
		String normalized = IsbnValidator.normalize(rawIsbn);
		if (!IsbnValidator.isValid(normalized)) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ISBN",
					"ISBN format is invalid.");
		}
		boolean duplicate = excludeProjectId == null
				? projectRepository.existsByIsbnAndDeletedAtIsNull(normalized)
				: projectRepository.existsByIsbnAndDeletedAtIsNullAndIdNot(normalized, excludeProjectId);
		if (duplicate) {
			throw new ConflictException("DUPLICATE_ISBN", "A project with this ISBN already exists.");
		}
		return normalized;
	}
}
