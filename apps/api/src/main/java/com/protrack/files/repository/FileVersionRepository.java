package com.protrack.files.repository;

import com.protrack.files.domain.FileVersion;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for {@link FileVersion} (immutable versions of a document). */
public interface FileVersionRepository extends JpaRepository<FileVersion, UUID> {

	List<FileVersion> findByDocumentIdOrderByVersionNoDesc(UUID documentId);

	List<FileVersion> findByDocumentIdInOrderByVersionNoDesc(Collection<UUID> documentIds);

	Optional<FileVersion> findFirstByDocumentIdOrderByVersionNoDesc(UUID documentId);

	Optional<FileVersion> findByDocumentIdAndCurrentTrue(UUID documentId);

	/**
	 * Clear the current flag for a document's versions. Must run (and flush) before a new current
	 * version is inserted so the partial-unique index ({@code one current per document}) is honored.
	 */
	@Modifying
	@Query("UPDATE FileVersion v SET v.current = false WHERE v.documentId = :documentId AND v.current = true")
	int clearCurrentForDocument(@Param("documentId") UUID documentId);
}
