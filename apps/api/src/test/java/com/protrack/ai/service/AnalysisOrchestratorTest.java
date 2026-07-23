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
 * Unit tests for {@link AnalysisOrchestrator} (Mockito; no Docker/Spring context). Regression
 * coverage for a real bug found during manual QA: the "Run AI analysis" button had no
 * client-side debounce, and the server had no matching guard, so a fast double-click created two
 * independent {@code AiJob}s for the same project — each firing its own real, billed LLM call and
 * its own duplicate STAGE_CHANGED/ANALYSIS_STARTED/ANALYSIS_COMPLETED audit trail entry (verified
 * live: two INTAKE→AI_ANALYSIS transitions logged for one user action). Fix: reject a new job
 * with 409 CONFLICT while one is already QUEUED or RUNNING for the same project.
 */
class AnalysisOrchestratorTest {

	private AiJobRepository aiJobRepository;
	private FilesFacade filesFacade;
	private ProjectFacade projectFacade;
	private AiJobDispatcher dispatcher;
	private AnalysisOrchestrator orchestrator;

	private final UUID projectId = UUID.randomUUID();
	private final UUID actor = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		aiJobRepository = mock(AiJobRepository.class);
		filesFacade = mock(FilesFacade.class);
		projectFacade = mock(ProjectFacade.class);
		dispatcher = mock(AiJobDispatcher.class);
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
		orchestrator = new AnalysisOrchestrator(
				aiJobRepository, filesFacade, projectFacade, dispatcher, eventPublisher);

		when(projectFacade.findContext(projectId)).thenReturn(Optional.of(
				new ProjectContextInfo(projectId, UUID.randomUUID(), "Title", "STEM_TEXTBOOK", null, "INTAKE")));
		when(filesFacade.listCurrentFilesForProject(projectId)).thenReturn(List.of(
				new FileRef(UUID.randomUUID(), UUID.randomUUID(), "MANUSCRIPT", "t", "f.docx",
						"application/vnd.openxmlformats-officedocument.wordprocessingml.document", 10L, "key")));
	}

	@AfterEach
	void tearDown() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	void rejectsANewAnalysisWhileOneIsAlreadyQueuedOrRunningForTheProject() {
		when(aiJobRepository.existsByProjectIdAndJobTypeAndStatusIn(
				any(), anyString(), any())).thenReturn(true);

		assertThatThrownBy(() -> orchestrator.startManuscriptAnalysis(actor, projectId))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("ANALYSIS_IN_PROGRESS"));

		verify(aiJobRepository, never()).save(any());
		verify(dispatcher, never()).dispatch(any());
	}

	@Test
	void startsANewAnalysisWhenNoneIsActiveForTheProject() {
		when(aiJobRepository.existsByProjectIdAndJobTypeAndStatusIn(
				any(), anyString(), any())).thenReturn(false);
		when(aiJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		// startManuscriptAnalysis registers a commit-time hook, which requires an active
		// transaction synchronization even outside a real Spring transaction.
		TransactionSynchronizationManager.initSynchronization();

		orchestrator.startManuscriptAnalysis(actor, projectId);

		verify(aiJobRepository).save(any());
	}
}
