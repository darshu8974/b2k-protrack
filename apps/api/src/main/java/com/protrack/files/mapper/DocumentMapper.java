package com.protrack.files.mapper;

import com.protrack.files.domain.Document;
import com.protrack.files.domain.FileVersion;
import com.protrack.files.web.dto.DocumentResponse;
import com.protrack.files.web.dto.DocumentSummaryResponse;
import com.protrack.files.web.dto.FileVersionResponse;
import org.springframework.stereotype.Component;

/** Maps files entities to response DTOs (uploader display names are supplied by the service). */
@Component
public class DocumentMapper {

	public FileVersionResponse toVersionResponse(FileVersion version, String uploadedByName) {
		if (version == null) {
			return null;
		}
		return new FileVersionResponse(
				version.getId().toString(),
				version.getVersionNo(),
				version.getFileName(),
				version.getMimeType(),
				version.getSizeBytes(),
				version.getChecksumSha256(),
				version.isCurrent(),
				version.getUploadedBy() == null ? null : version.getUploadedBy().toString(),
				uploadedByName,
				version.getCreatedAt());
	}

	public DocumentResponse toResponse(Document document, FileVersion currentVersion,
			String currentUploaderName) {
		return new DocumentResponse(
				document.getId().toString(),
				document.getProjectId().toString(),
				document.getDocType(),
				document.getTitle(),
				document.getStatus(),
				toVersionResponse(currentVersion, currentUploaderName),
				document.getCreatedAt(),
				document.getUpdatedAt());
	}

	public DocumentSummaryResponse toSummary(Document document, FileVersion currentVersion,
			int versionCount, String currentUploaderName) {
		return new DocumentSummaryResponse(
				document.getId().toString(),
				document.getDocType(),
				document.getTitle(),
				document.getStatus(),
				versionCount,
				toVersionResponse(currentVersion, currentUploaderName),
				document.getCreatedAt(),
				document.getUpdatedAt());
	}
}
