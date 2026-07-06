import { apiClient } from "../../api/axios";
import type { Page } from "../../types/api";
import type { Notification, NotificationPreference, UnreadCount } from "../../types/notification";

export async function listNotifications(
  unreadOnly: boolean,
  page = 0,
  size = 20,
): Promise<Page<Notification>> {
  const { data } = await apiClient.get<Page<Notification>>("/notifications", {
    params: { unread: unreadOnly, page, size },
  });
  return data;
}

export async function getUnreadCount(): Promise<UnreadCount> {
  const { data } = await apiClient.get<UnreadCount>("/notifications/unread-count");
  return data;
}

export async function markNotificationRead(id: string): Promise<void> {
  await apiClient.post(`/notifications/${id}:read`);
}

export async function markAllNotificationsRead(): Promise<void> {
  await apiClient.post("/notifications:read-all");
}

export async function getNotificationPreferences(): Promise<NotificationPreference[]> {
  const { data } = await apiClient.get<NotificationPreference[]>("/notification-preferences");
  return data;
}

export async function updateNotificationPreferences(
  preferences: Array<Pick<NotificationPreference, "type" | "inAppEnabled" | "emailEnabled">>,
): Promise<NotificationPreference[]> {
  const { data } = await apiClient.patch<NotificationPreference[]>("/notification-preferences", {
    preferences,
  });
  return data;
}
