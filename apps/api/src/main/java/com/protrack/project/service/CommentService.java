package com.protrack.project.service;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.project.domain.Comment;
import com.protrack.project.repository.CommentRepository;
import com.protrack.project.repository.ProjectRepository;
import com.protrack.project.web.dto.CommentResponse;
import com.protrack.project.web.dto.CreateCommentRequest;
import com.protrack.project.web.dto.UpdateCommentRequest;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.security.AuthorizationService;
import com.protrack.shared.web.PageResponse;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Threaded project comments. Lives in the project module (comments are project-scoped and the
 * Database Design's module map has no separate comment module), so membership is checked directly
 * against {@link ProjectRepository} — no cross-module SPI needed. Authorization: read/add require
 * project membership (ADMIN overrides); edit is author-only; delete is author-or-ADMIN.
 */
@Service
public class CommentService {

	private static final String ROLE_ADMIN = "ADMIN";
	private static final String CONTEXT_PROJECT = "PROJECT";

	private final CommentRepository commentRepository;
	private final ProjectRepository projectRepository;
	private final IdentityFacade identityFacade;
	private final AuthorizationService authorizationService;

	public CommentService(CommentRepository commentRepository, ProjectRepository projectRepository,
			IdentityFacade identityFacade, AuthorizationService authorizationService) {
		this.commentRepository = commentRepository;
		this.projectRepository = projectRepository;
		this.identityFacade = identityFacade;
		this.authorizationService = authorizationService;
	}

	@Transactional(readOnly = true)
	public PageResponse<CommentResponse> list(UUID currentUserId, UUID projectId, String contextType,
			UUID contextId, Pageable pageable) {
		assertProjectAccess(projectId, currentUserId);
		Page<Comment> page = commentRepository.findFeed(projectId,
				StringUtils.hasText(contextType) ? contextType : null, contextId, pageable);
		Map<UUID, UserBrief> authors = resolveAuthors(page.getContent().stream()
				.map(Comment::getAuthorId).collect(Collectors.toSet()));
		return PageResponse.of(page.map(comment -> toResponse(comment, authors.get(comment.getAuthorId()))));
	}

	@Transactional
	public CommentResponse add(UUID currentUserId, UUID projectId, CreateCommentRequest request) {
		assertProjectAccess(projectId, currentUserId);

		UUID parentId = validateParent(request.parentId(), projectId);
		String contextType = StringUtils.hasText(request.contextType())
				? request.contextType() : CONTEXT_PROJECT;
		// Default the context to the project itself so general discussion filters uniformly.
		UUID contextId = request.contextId() != null ? request.contextId()
				: (CONTEXT_PROJECT.equals(contextType) ? projectId : null);

		Comment comment = new Comment(UUID.randomUUID(), projectId, parentId, currentUserId,
				contextType, contextId, request.body(), Instant.now());
		commentRepository.save(comment);
		return toResponse(comment, identityFacade.findBrief(currentUserId).orElse(null));
	}

	@Transactional
	public CommentResponse edit(UUID currentUserId, UUID commentId, UpdateCommentRequest request) {
		Comment comment = liveComment(commentId);
		if (!comment.getAuthorId().equals(currentUserId)) {
			throw forbidden("Only the author can edit this comment.");
		}
		comment.editBody(request.body(), Instant.now());
		return toResponse(comment, identityFacade.findBrief(comment.getAuthorId()).orElse(null));
	}

	@Transactional
	public void delete(UUID currentUserId, UUID commentId) {
		Comment comment = liveComment(commentId);
		boolean isAuthor = comment.getAuthorId().equals(currentUserId);
		if (!isAuthor && !authorizationService.hasAnyRole(ROLE_ADMIN)) {
			throw forbidden("Only the author or an administrator can delete this comment.");
		}
		comment.softDelete(Instant.now());
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private void assertProjectAccess(UUID projectId, UUID currentUserId) {
		if (projectRepository.findByIdAndDeletedAtIsNull(projectId).isEmpty()) {
			throw new NotFoundException("Project not found.");
		}
		if (!authorizationService.hasAnyRole(ROLE_ADMIN)
				&& !projectRepository.isMember(projectId, currentUserId)) {
			throw forbidden("You must be a member of this project to view or add comments.");
		}
	}

	private UUID validateParent(UUID parentId, UUID projectId) {
		if (parentId == null) {
			return null;
		}
		Comment parent = commentRepository.findByIdAndDeletedAtIsNull(parentId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_PARENT",
						"Parent comment not found."));
		if (!parent.getProjectId().equals(projectId)) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_PARENT",
					"Parent comment belongs to a different project.");
		}
		return parentId;
	}

	private Comment liveComment(UUID commentId) {
		return commentRepository.findByIdAndDeletedAtIsNull(commentId)
				.orElseThrow(() -> new NotFoundException("Comment not found."));
	}

	private Map<UUID, UserBrief> resolveAuthors(Set<UUID> authorIds) {
		return identityFacade.findBriefs(authorIds.stream().filter(Objects::nonNull).toList());
	}

	private static ApiException forbidden(String message) {
		return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
	}

	private static CommentResponse toResponse(Comment c, UserBrief author) {
		return new CommentResponse(
				c.getId().toString(),
				c.getProjectId().toString(),
				c.getParentCommentId() == null ? null : c.getParentCommentId().toString(),
				c.getAuthorId().toString(),
				author == null ? null : author.fullName(),
				author == null ? null : author.avatarInitials(),
				author == null ? null : author.avatarColor(),
				c.getContextType(),
				c.getContextId() == null ? null : c.getContextId().toString(),
				c.getBody(),
				c.isEdited(),
				c.getCreatedAt(),
				c.getUpdatedAt());
	}
}
