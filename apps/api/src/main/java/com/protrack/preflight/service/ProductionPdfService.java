package com.protrack.preflight.service;

import com.protrack.files.spi.FilesFacade;
import com.protrack.files.spi.FilesFacade.FileRef;
import com.protrack.preflight.web.dto.ProductionPdfResponse;
import com.protrack.workflow.service.WorkflowService;
import com.protrack.workflow.web.dto.TransitionResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles the designer's production-PDF hand-off: stores the PDF (files module) and advances the
 * project {@code IN_PRODUCTION → PDF_REVIEW} (workflow engine, {@code SUBMIT_PDF} rule) in one
 * transaction. If the project is not in {@code IN_PRODUCTION}, the transition rejects (409) and the
 * upload rolls back with it, so the two stay consistent.
 */
@Service
public class ProductionPdfService {

	private static final String PDF_REVIEW_STAGE = "PDF_REVIEW";

	private final FilesFacade filesFacade;
	private final WorkflowService workflowService;

	public ProductionPdfService(FilesFacade filesFacade, WorkflowService workflowService) {
		this.filesFacade = filesFacade;
		this.workflowService = workflowService;
	}

	@Transactional
	public ProductionPdfResponse submit(UUID actorId, UUID projectId, MultipartFile file,
			String title) {
		FileRef ref = filesFacade.uploadProductionPdf(actorId, projectId, file, title);
		TransitionResponse transition = workflowService.transition(
				actorId, projectId, PDF_REVIEW_STAGE, "Production PDF uploaded");
		return new ProductionPdfResponse(
				ref.documentId().toString(), ref.versionId().toString(), ref.fileName(),
				transition.toStage());
	}
}
