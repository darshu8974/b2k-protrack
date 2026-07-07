package com.protrack.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import com.protrack.notification.domain.Notification;
import com.protrack.notification.domain.NotificationPreference;
import com.protrack.notification.domain.NotificationType;
import com.protrack.notification.repository.NotificationPreferenceRepository;
import com.protrack.notification.repository.NotificationRepository;
import com.protrack.shared.error.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link NotificationService} fan-out and read state (Mockito; no Docker). */
class NotificationServiceTest {

	private NotificationRepository notificationRepository;
	private NotificationPreferenceRepository preferenceRepository;
	private NotificationDispatcher dispatcher;
	private IdentityFacade identityFacade;
	private NotificationService service;

	private final UUID alice = UUID.randomUUID();
	private final UUID bob = UUID.randomUUID();
	private final UUID projectId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		notificationRepository = mock(NotificationRepository.class);
		preferenceRepository = mock(NotificationPreferenceRepository.class);
		dispatcher = mock(NotificationDispatcher.class);
		identityFacade = mock(IdentityFacade.class);
		service = new NotificationService(notificationRepository, preferenceRepository, dispatcher,
				identityFacade);
		when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
	}

	private NotificationRequest request(UUID... recipients) {
		return new NotificationRequest(NotificationType.STAGE_CHANGED, List.of(recipients),
				"Stage changed", "The project advanced.", projectId, "PROJECT", projectId);
	}

	private void briefsWithEmail(UUID... ids) {
		Map<UUID, UserBrief> briefs = new java.util.HashMap<>();
		for (UUID id : ids) {
			briefs.put(id, new UserBrief(id, "User " + id, id + "@protrack.io", "US", "#000"));
		}
		when(identityFacade.findBriefs(any())).thenReturn(briefs);
	}

	@Test
	void defaultPreferencesDeliverInAppAndEmailToEveryRecipient() {
		briefsWithEmail(alice, bob);
		when(preferenceRepository.findByUserIdInAndType(anyList(), eq("STAGE_CHANGED")))
				.thenReturn(List.of()); // no stored prefs -> default on

		service.publish(request(alice, bob));

		verify(notificationRepository, org.mockito.Mockito.times(2)).save(any(Notification.class));
		verify(dispatcher).deliverEmail(eq(alice + "@protrack.io"), any(), any());
		verify(dispatcher).deliverEmail(eq(bob + "@protrack.io"), any(), any());
	}

	@Test
	void mutedInAppSuppressesBothChannels() {
		briefsWithEmail(alice);
		// Alice muted in-app for this type -> no row, no email (email-only delivery is Phase 2).
		when(preferenceRepository.findByUserIdInAndType(anyList(), eq("STAGE_CHANGED")))
				.thenReturn(List.of(new NotificationPreference(UUID.randomUUID(), alice,
						NotificationType.STAGE_CHANGED, false, true)));

		service.publish(request(alice));

		verify(notificationRepository, never()).save(any());
		verify(dispatcher, never()).deliverEmail(any(), any(), any());
	}

	@Test
	void emailDisabledStillWritesTheInAppRow() {
		briefsWithEmail(alice);
		when(preferenceRepository.findByUserIdInAndType(anyList(), eq("STAGE_CHANGED")))
				.thenReturn(List.of(new NotificationPreference(UUID.randomUUID(), alice,
						NotificationType.STAGE_CHANGED, true, false)));

		service.publish(request(alice));

		verify(notificationRepository).save(any(Notification.class));
		verify(dispatcher, never()).deliverEmail(any(), any(), any());
	}

	@Test
	void emptyRecipientsIsANoOp() {
		service.publish(request());
		verify(notificationRepository, never()).save(any());
		verify(identityFacade, never()).findBriefs(any());
	}

	@Test
	void markReadOnAForeignNotificationIs404() {
		UUID notificationId = UUID.randomUUID();
		when(notificationRepository.findByIdAndRecipientId(notificationId, alice))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.markRead(alice, notificationId))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void unreadCountDelegatesToRepository() {
		when(notificationRepository.countByRecipientIdAndReadFalse(alice)).thenReturn(3L);
		assertThat(service.unreadCount(alice)).isEqualTo(3L);
	}
}
