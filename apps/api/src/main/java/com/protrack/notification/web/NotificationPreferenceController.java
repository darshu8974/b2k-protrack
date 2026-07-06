package com.protrack.notification.web;

import com.protrack.notification.service.NotificationService;
import com.protrack.notification.web.dto.NotificationPreferenceResponse;
import com.protrack.notification.web.dto.UpdatePreferencesRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The current user's per-type notification channel preferences (any authenticated role). Absence of
 * a stored row means both channels are enabled (default-on); {@code GET} always returns the full
 * set of known types with effective values.
 */
@RestController
@RequestMapping("/api/v1")
public class NotificationPreferenceController {

	private final NotificationService notificationService;

	public NotificationPreferenceController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/notification-preferences")
	public List<NotificationPreferenceResponse> get(Principal principal) {
		return notificationService.getPreferences(currentUser(principal));
	}

	@PatchMapping("/notification-preferences")
	public List<NotificationPreferenceResponse> update(
			@Valid @RequestBody UpdatePreferencesRequest request, Principal principal) {
		return notificationService.updatePreferences(currentUser(principal), request);
	}

	private static UUID currentUser(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
