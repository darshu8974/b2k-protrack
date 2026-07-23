package com.protrack.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.protrack.ai.repository.AiJobRepository;
import com.protrack.files.spi.FilesFacade;
import com.protrack.files.spi.FilesFacade.FileRef;
import com.protrack.project.spi.ProjectFacade;
import com.protrack.project.spi.ProjectFacade.ProjectContextInfo;
import com.protrack.shared.error.ApiException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Unit tests for {@link PreflightOrchestrator} (Mockito; no Docker/Spring context). Mirrors
 * {@link AnalysisOrchestratorTest} — same duplicate-submission bug, same fix, applied to the
 * "Run preflight" button which had the identical missing-guard defect.
 */
class PreflightOrchestratorTest {

	private AiJobRepository aiJobRepository;
	private FilesFacade filesFacade;
	private ProjectFacade projectFacade;
	private AiJobDispatcher dispatcher;
	private PreflightOrchestrator orchestrator;

	private final UUID projectId = UUID.randomUUID();
	private final UUID actor = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		aiJobRepository = mock(AiJobRepository.class);
		filesFacade = mock(FilesFacade.class);
		projectFacade = mock(ProjectFacade.class);
		dispatcher = mock(AiJobDispatcher.class);
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
		orchestrator = new PreflightOrchestrator(
				aiJobRepository, filesFacade, projectFacade, dispatcher, eventPublisher);

		when(projectFacade.findContext(projectId)).thenReturn(Optional.of(
				new ProjectContextInfo(projectId, UUID.randomUUID(), "Title", "STEM_TEXTBOOK", null, "PDF_REVIEW")));
		when(filesFacade.listCurrentFilesForProject(projectId)).thenReturn(List.of(
				new FileRef(UUID.randomUUID(), UUID.randomUUID(), "PRODUCTION_PDF", "t", "f.pdf",
						"application/pdf", 10L, "key")));
	}

	@AfterEach
	void tearDown() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	void rejectsANewPreflightWhileOneIsAlreadyQueuedOrRunningForTheProject() {
		when(aiJobRepository.existsByProjectIdAndJobTypeAndStatusIn(
				any(), anyString(), any())).thenReturn(true);

		assertThatThrownBy(() -> orchestrator.startPreflight(actor, projectId))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("PREFLIGHT_IN_PROGRESS"));

		verify(aiJobRepository, never()).save(any());
		verify(dispatcher, never()).dispatch(any());
	}

	@Test
	void startsANewPreflightWhenNoneIsActiveForTheProject() {
		when(aiJobRepository.existsByProjectIdAndJobTypeAndStatusIn(
				any(), anyString(), any())).thenReturn(false);
		when(aiJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		TransactionSynchronizationManager.initSynchronization();

		orchestrator.startPreflight(actor, projectId);

		verify(aiJobRepository).save(any());
	}
}
