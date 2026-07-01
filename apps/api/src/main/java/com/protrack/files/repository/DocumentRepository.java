package com.protrack.files.repository;

import com.protrack.files.domain.Document;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Document}. */
public interface DocumentRepository extends JpaRepository<Document, UUID> {

	Optional<Document> findByIdAndDeletedAtIsNull(UUID id);

	List<Document> findByProjectIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID projectId);

	List<Document> findByProjectIdAndDocTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
			UUID projectId, String docType);
}
