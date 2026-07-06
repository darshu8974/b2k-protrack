package com.protrack.notification.service;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.notification.domain.Notification;
import com.protrack.notification.domain.NotificationChannel;
import com.protrack.notification.domain.NotificationPreference;
import com.protrack.notification.domain.NotificationType;
import com.protrack.notification.repository.NotificationPreferenceRepository;
import com.protrack.notification.repository.NotificationRepository;
import com.protrack.notification.web.dto.NotificationPreferenceResponse;
import com.protrack.notification.web.dto.NotificationResponse;
import com.protrack.notification.web.dto.UpdatePreferencesRequest;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.error.NotFoundException;
import com.protrack.shared.web.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates and reads notifications. Fan-out ({@link #publish}) runs in its own transaction so a
 * notification failure never rolls back the business operation that triggered it (the event
 * listener invokes this AFTER_COMMIT). Each recipient gets an in-app feed row when their in-app
 * preference is enabled; email piggybacks on that row (best-effort, off the request thread) when
 * their email preference is also enabled.
 */
@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final NotificationRepository notificationRepository;
	private final NotificationPreferenceRepository preferenceRepository;
	private final NotificationDispatcher dispatcher;
	private final IdentityFacade identityFacade;

	public NotificationService(NotificationRepository notificationRepository,
			NotificationPreferenceRepository preferenceRepository, NotificationDispatcher dispatcher,
			IdentityFacade identityFacade) {
		this.notificationRepository = notificationRepository;
		this.preferenceRepository = preferenceRepository;
		this.dispatcher = dispatcher;
		this.identityFacade = identityFacade;
	}

	// ── Fan-out ────────────────────────────────────────────────────────────────

	/**
	 * Deliver a notification to each recipient per their channel preferences. Runs in a new
	 * transaction (REQUIRES_NEW): called after the originating business transaction has committed,
	 * it must isolate its own success/failure from that operation.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void publish(NotificationRequest request) {
		List<UUID> recipients = request.recipientIds();
		if (recipients == null || recipients.isEmpty()) {
			return;
		}
		Map<UUID, UserBrief> briefs = identityFacade.findBriefs(recipients);
		Map<UUID, NotificationPreference> prefs = preferenceRepository
				.findByUserIdInAndType(recipients, request.type().name()).stream()
				.collect(Collectors.toMap(NotificationPreference::getUserId, Function.identity()));
		Instant now = Instant.now();

		for (UUID recipientId : recipients) {
			NotificationPreference pref = prefs.get(recipientId);
			boolean inApp = pref == null || pref.isInAppEnabled();
			boolean email = pref == null || pref.isEmailEnabled();
			if (!inApp) {
				// In-app is the durable record; if a user muted in-app for this type, skip entirely
				// (independent email-only delivery is deferred to Phase 2).
				continue;
			}
			UserBrief brief = briefs.get(recipientId);
			boolean sendEmail = email && brief != null && brief.email() != null;

			notificationRepository.save(new Notification(UUID.randomUUID(), recipientId,
					request.projectId(), request.type(), request.title(), request.body(),
					NotificationChannel.IN_APP, request.relatedEntityType(), request.relatedEntityId(),
					sendEmail ? now : null));

			if (sendEmail) {
				dispatcher.deliverEmail(brief.email(), request.title(), request.body());
			}
		}
		log.debug("Notified {} recipient(s) of {}", recipients.size(), request.type());
	}

	// ── Feed / read state ────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public PageResponse<NotificationResponse> feed(UUID currentUserId, boolean unreadOnly,
			Pageable pageable) {
		Page<Notification> page = unreadOnly
				? notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(
						currentUserId, pageable)
				: notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUserId, pageable);
		return PageResponse.of(page.map(NotificationService::toResponse));
	}

	@Transactional(readOnly = true)
	public long unreadCount(UUID currentUserId) {
		return notificationRepository.countByRecipientIdAndReadFalse(currentUserId);
	}

	@Transactional
	public void markRead(UUID currentUserId, UUID notificationId) {
		Notification notification = notificationRepository
				.findByIdAndRecipientId(notificationId, currentUserId)
				.orElseThrow(() -> new NotFoundException("Notification not found."));
		notification.markRead(Instant.now());
	}

	@Transactional
	public void markAllRead(UUID currentUserId) {
		notificationRepository.markAllRead(currentUserId, Instant.now());
	}

	// ── Preferences ───────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public List<NotificationPreferenceResponse> getPreferences(UUID currentUserId) {
		Map<String, NotificationPreference> stored = preferenceRepository.findByUserId(currentUserId)
				.stream().collect(Collectors.toMap(NotificationPreference::getType, Function.identity()));
		return List.of(NotificationType.values()).stream()
				.map(type -> toPreferenceResponse(type, stored.get(type.name())))
				.toList();
	}

	@Transactional
	public List<NotificationPreferenceResponse> updatePreferences(UUID currentUserId,
			UpdatePreferencesRequest request) {
		for (UpdatePreferencesRequest.Item item : request.preferences()) {
			NotificationType type = parseType(item.type());
			NotificationPreference existing = preferenceRepository
					.findByUserIdAndType(currentUserId, type.name()).orElse(null);
			if (existing == null) {
				preferenceRepository.save(new NotificationPreference(UUID.randomUUID(), currentUserId,
						type, item.inAppEnabled(), item.emailEnabled()));
			} else {
				existing.update(item.inAppEnabled(), item.emailEnabled());
			}
		}
		return getPreferences(currentUserId);
	}

	private static NotificationType parseType(String raw) {
		try {
			return NotificationType.valueOf(raw);
		} catch (IllegalArgumentException ex) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_NOTIFICATION_TYPE",
					"Unknown notification type: " + raw);
		}
	}

	private static NotificationPreferenceResponse toPreferenceResponse(NotificationType type,
			NotificationPreference pref) {
		boolean inApp = pref == null || pref.isInAppEnabled();
		boolean email = pref == null || pref.isEmailEnabled();
		return new NotificationPreferenceResponse(type.name(), type.label(), inApp, email);
	}

	private static NotificationResponse toResponse(Notification n) {
		return new NotificationResponse(
				n.getId().toString(),
				n.getType(),
				n.getTitle(),
				n.getBody(),
				n.getRelatedProjectId() == null ? null : n.getRelatedProjectId().toString(),
				n.getRelatedEntityType(),
				n.getRelatedEntityId() == null ? null : n.getRelatedEntityId().toString(),
				n.isRead(),
				n.getReadAt(),
				n.getSentAt(),
				n.getCreatedAt());
	}
}
