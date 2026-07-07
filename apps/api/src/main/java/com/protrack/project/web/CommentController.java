package com.protrack.project.web;

import com.protrack.project.service.CommentService;
import com.protrack.project.web.dto.CommentResponse;
import com.protrack.project.web.dto.CreateCommentRequest;
import com.protrack.project.web.dto.UpdateCommentRequest;
import com.protrack.shared.web.PageResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Threaded project comments (API Specification §3.12). Authorization is contextual and enforced in
 * {@link CommentService}: read/add require project membership (ADMIN overrides), edit is author-only,
 * delete is author-or-ADMIN.
 */
@RestController
@RequestMapping("/api/v1")
public class CommentController {

	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	@GetMapping("/projects/{projectId}/comments")
	public PageResponse<CommentResponse> list(
			@PathVariable UUID projectId,
			@RequestParam(required = false) String contextType,
			@RequestParam(required = false) UUID contextId,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
			Pageable pageable,
			Principal principal) {
		return commentService.list(currentUser(principal), projectId, contextType, contextId, pageable);
	}

	@PostMapping("/projects/{projectId}/comments")
	@ResponseStatus(HttpStatus.CREATED)
	public CommentResponse add(@PathVariable UUID projectId,
			@Valid @RequestBody CreateCommentRequest request, Principal principal) {
		return commentService.add(currentUser(principal), projectId, request);
	}

	@PatchMapping("/comments/{id}")
	public CommentResponse edit(@PathVariable UUID id,
			@Valid @RequestBody UpdateCommentRequest request, Principal principal) {
		return commentService.edit(currentUser(principal), id, request);
	}

	@DeleteMapping("/comments/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) {
		commentService.delete(currentUser(principal), id);
		return ResponseEntity.noContent().build();
	}

	private static UUID currentUser(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
