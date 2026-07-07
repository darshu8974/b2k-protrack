package com.protrack.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.project.domain.Comment;
import com.protrack.project.domain.Project;
import com.protrack.project.repository.CommentRepository;
import com.protrack.project.repository.ProjectRepository;
import com.protrack.project.web.dto.CommentResponse;
import com.protrack.project.web.dto.CreateCommentRequest;
import com.protrack.project.web.dto.UpdateCommentRequest;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.security.AuthorizationService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CommentService} threading + authorization (Mockito; no Docker). */
class CommentServiceTest {

	private CommentRepository commentRepository;
	private ProjectRepository projectRepository;
	private AuthorizationService authorizationService;
	private CommentService service;

	private final UUID author = UUID.randomUUID();
	private final UUID projectId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		commentRepository = mock(CommentRepository.class);
		projectRepository = mock(ProjectRepository.class);
		IdentityFacade identityFacade = mock(IdentityFacade.class);
		authorizationService = mock(AuthorizationService.class);
		service = new CommentService(commentRepository, projectRepository, identityFacade,
				authorizationService);

		when(projectRepository.findByIdAndDeletedAtIsNull(projectId))
				.thenReturn(Optional.of(mock(Project.class)));
		when(identityFacade.findBrief(any())).thenReturn(Optional.empty());
		when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
	}

	private void memberAccess() {
		when(authorizationService.hasAnyRole("ADMIN")).thenReturn(false);
		when(projectRepository.isMember(projectId, author)).thenReturn(true);
	}

	@Test
	void memberCanAddARootComment() {
		memberAccess();

		CommentResponse response = service.add(author, projectId,
				new CreateCommentRequest("Looks good", null, null, null));

		assertThat(response.body()).isEqualTo("Looks good");
		assertThat(response.parentId()).isNull();
		verify(commentRepository).save(any(Comment.class));
	}

	@Test
	void replyToAValidParentIsThreaded() {
		memberAccess();
		UUID parentId = UUID.randomUUID();
		Comment parent = new Comment(parentId, projectId, null, author, "PROJECT", projectId,
				"root", Instant.now());
		when(commentRepository.findByIdAndDeletedAtIsNull(parentId)).thenReturn(Optional.of(parent));

		CommentResponse response = service.add(author, projectId,
				new CreateCommentRequest("A reply", parentId, null, null));

		assertThat(response.parentId()).isEqualTo(parentId.toString());
	}

	@Test
	void replyToAMissingParentIs422() {
		memberAccess();
		UUID parentId = UUID.randomUUID();
		when(commentRepository.findByIdAndDeletedAtIsNull(parentId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.add(author, projectId,
				new CreateCommentRequest("orphan", parentId, null, null)))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("INVALID_PARENT"));
	}

	@Test
	void replyToAParentInAnotherProjectIs422() {
		memberAccess();
		UUID parentId = UUID.randomUUID();
		Comment foreign = new Comment(parentId, UUID.randomUUID(), null, author, "PROJECT", null,
				"root", Instant.now());
		when(commentRepository.findByIdAndDeletedAtIsNull(parentId)).thenReturn(Optional.of(foreign));

		assertThatThrownBy(() -> service.add(author, projectId,
				new CreateCommentRequest("cross", parentId, null, null)))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("INVALID_PARENT"));
	}

	@Test
	void nonMemberCannotAddAComment() {
		when(authorizationService.hasAnyRole("ADMIN")).thenReturn(false);
		when(projectRepository.isMember(projectId, author)).thenReturn(false);

		assertThatThrownBy(() -> service.add(author, projectId,
				new CreateCommentRequest("hi", null, null, null)))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("FORBIDDEN"));
		verify(commentRepository, never()).save(any());
	}

	@Test
	void onlyTheAuthorMayEdit() {
		UUID commentId = UUID.randomUUID();
		Comment comment = new Comment(commentId, projectId, null, author, "PROJECT", projectId,
				"mine", Instant.now());
		when(commentRepository.findByIdAndDeletedAtIsNull(commentId)).thenReturn(Optional.of(comment));

		assertThatThrownBy(() -> service.edit(UUID.randomUUID(), commentId,
				new UpdateCommentRequest("hacked")))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("FORBIDDEN"));
	}

	@Test
	void authorCanSoftDeleteOwnComment() {
		UUID commentId = UUID.randomUUID();
		Comment comment = new Comment(commentId, projectId, null, author, "PROJECT", projectId,
				"mine", Instant.now());
		when(commentRepository.findByIdAndDeletedAtIsNull(commentId)).thenReturn(Optional.of(comment));

		service.delete(author, commentId);

		assertThat(comment.isDeleted()).isTrue();
	}
}
