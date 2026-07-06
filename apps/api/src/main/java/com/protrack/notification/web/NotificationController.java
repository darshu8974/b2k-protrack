package com.protrack.notification.web;

import com.protrack.notification.service.NotificationService;
import com.protrack.notification.web.dto.NotificationResponse;
import com.protrack.notification.web.dto.UnreadCountResponse;
import com.protrack.shared.web.PageResponse;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The current user's notification feed and read-state actions (any authenticated role). Ownership
 * is implicit: every operation is scoped to the caller ({@code Principal} = user id), so a user can
 * only see and mutate their own notifications.
 */
@RestController
@RequestMapping("/api/v1")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/notifications")
	public PageResponse<NotificationResponse> list(
			@RequestParam(name = "unread", defaultValue = "false") boolean unreadOnly,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			Principal principal) {
		return notificationService.feed(currentUser(principal), unreadOnly, pageable);
	}

	@GetMapping("/notifications/unread-count")
	public UnreadCountResponse unreadCount(Principal principal) {
		return new UnreadCountResponse(notificationService.unreadCount(currentUser(principal)));
	}

	@PostMapping("/notifications/{id}:read")
	public ResponseEntity<Void> markRead(@PathVariable UUID id, Principal principal) {
		notificationService.markRead(currentUser(principal), id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/notifications:read-all")
	public ResponseEntity<Void> markAllRead(Principal principal) {
		notificationService.markAllRead(currentUser(principal));
		return ResponseEntity.noContent().build();
	}

	private static UUID currentUser(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
