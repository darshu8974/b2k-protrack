package com.protrack.project.spi;

import com.protrack.project.domain.Project;
import com.protrack.project.domain.ProjectStatus;
import com.protrack.project.repository.ProjectRepository;
import com.protrack.shared.error.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link ProjectFacade} backed by {@link ProjectRepository}. */
@Service
public class ProjectFacadeImpl implements ProjectFacade {

	private static final String COMPLETED_STAGE = "COMPLETED";

	private final ProjectRepository projectRepository;

	public ProjectFacadeImpl(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ProjectStageInfo> findStageInfo(UUID projectId) {
		return projectRepository.findByIdAndDeletedAtIsNull(projectId)
				.map(project -> new ProjectStageInfo(
						project.getId(), project.getCurrentStage(),
						project.getOrganizationId(), project.getOwnerId()));
	}

	@Override
	@Transactional
	public void updateCurrentStage(UUID projectId, String newStage, UUID actedBy) {
		Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
				.orElseThrow(() -> new NotFoundException("Project not found."));
		project.setCurrentStage(newStage);
		if (COMPLETED_STAGE.equals(newStage)) {
			project.setStatus(ProjectStatus.COMPLETED.name());
		}
		project.setUpdatedBy(actedBy);
		projectRepository.save(project);
	}
}
