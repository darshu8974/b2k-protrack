package com.protrack.project.mapper;

import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.project.domain.Imprint;
import com.protrack.project.domain.Project;
import com.protrack.project.domain.ProjectMember;
import com.protrack.project.web.dto.ImprintResponse;
import com.protrack.project.web.dto.ProjectMemberResponse;
import com.protrack.project.web.dto.ProjectResponse;
import com.protrack.project.web.dto.ProjectSummaryResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Maps project entities to response DTOs (user details are supplied by the service via facade). */
@Component
public class ProjectMapper {

	public ProjectResponse toResponse(Project project, UserBrief owner, Map<UUID, UserBrief> members) {
		List<ProjectMemberResponse> memberResponses = project.getMembers().stream()
				.map(member -> toMemberResponse(member, members.get(member.getUserId())))
				.toList();

		return new ProjectResponse(
				project.getId().toString(),
				project.getTitle(),
				project.getIsbn(),
				project.getPublicationType(),
				project.getDiscipline(),
				project.getBrief(),
				project.getPageExtent(),
				project.getTrimSize(),
				project.getPriority(),
				project.getCurrentStage(),
				project.getStatus(),
				project.getDueDate(),
				project.getCreatedDate(),
				project.getCreatedAt(),
				project.getUpdatedAt(),
				toImprintResponse(project.getImprint()),
				owner == null ? null
						: new ProjectResponse.OwnerResponse(owner.id().toString(), owner.fullName(), owner.email()),
				memberResponses);
	}

	public ProjectSummaryResponse toSummary(Project project, String ownerName) {
		return new ProjectSummaryResponse(
				project.getId().toString(),
				project.getTitle(),
				project.getIsbn(),
				project.getPublicationType(),
				project.getDiscipline(),
				project.getImprint() == null ? null : project.getImprint().getName(),
				project.getCurrentStage(),
				project.getStatus(),
				project.getPriority(),
				project.getDueDate(),
				ownerName);
	}

	public ProjectMemberResponse toMemberResponse(ProjectMember member, UserBrief brief) {
		return new ProjectMemberResponse(
				member.getUserId().toString(),
				brief == null ? null : brief.fullName(),
				brief == null ? null : brief.email(),
				brief == null ? null : brief.avatarInitials(),
				member.getRoleInProject(),
				member.isOwner(),
				member.getMatchScore());
	}

	private ImprintResponse toImprintResponse(Imprint imprint) {
		if (imprint == null) {
			return null;
		}
		return new ImprintResponse(imprint.getId().toString(), imprint.getName(), imprint.getCode());
	}
}
