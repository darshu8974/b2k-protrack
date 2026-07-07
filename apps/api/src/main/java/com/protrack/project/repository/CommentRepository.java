package com.protrack.project.repository;

import com.protrack.project.domain.Comment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for {@link Comment} (project-scoped threaded discussion, soft-deleted). */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

	/**
	 * A project's live comments, optionally filtered by polymorphic context. Excludes soft-deleted
	 * rows; ordered oldest-first so a thread reads chronologically (the client nests by parentId).
	 */
	@Query("""
			SELECT c FROM Comment c
			WHERE c.projectId = :projectId AND c.deletedAt IS NULL
			AND (:contextType IS NULL OR c.contextType = :contextType)
			AND (:contextId IS NULL OR c.contextId = :contextId)
			ORDER BY c.createdAt ASC""")
	Page<Comment> findFeed(@Param("projectId") UUID projectId,
			@Param("contextType") String contextType, @Param("contextId") UUID contextId,
			Pageable pageable);

	/** A live (non-deleted) comment by id — used for edit/delete and parent validation. */
	Optional<Comment> findByIdAndDeletedAtIsNull(UUID id);
}
