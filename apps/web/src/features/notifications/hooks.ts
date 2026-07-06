import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import {
  getNotificationPreferences,
  listNotifications,
  markAllNotificationsRead,
  markNotificationRead,
  updateNotificationPreferences,
} from "./api";
import type { NotificationPreference } from "../../types/notification";

export function useNotifications(unreadOnly: boolean, enabled = true) {
  return useQuery({
    queryKey: queryKeys.notifications(unreadOnly),
    queryFn: () => listNotifications(unreadOnly),
    enabled,
  });
}

/** Invalidate both the feed (read + unread views) and the badge count after a read mutation. */
function useInvalidateNotifications() {
  const queryClient = useQueryClient();
  return () => {
    void queryClient.invalidateQueries({ queryKey: ["notifications"] });
  };
}

export function useMarkRead() {
  const invalidate = useInvalidateNotifications();
  return useMutation({
    mutationFn: (id: string) => markNotificationRead(id),
    onSuccess: invalidate,
  });
}

export function useMarkAllRead() {
  const invalidate = useInvalidateNotifications();
  return useMutation({
    mutationFn: () => markAllNotificationsRead(),
    onSuccess: invalidate,
  });
}

export function useNotificationPreferences(enabled = true) {
  return useQuery({
    queryKey: queryKeys.notificationPreferences,
    queryFn: getNotificationPreferences,
    enabled,
  });
}

export function useUpdateNotificationPreferences() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (
      preferences: Array<Pick<NotificationPreference, "type" | "inAppEnabled" | "emailEnabled">>,
    ) => updateNotificationPreferences(preferences),
    onSuccess: (data) => {
      queryClient.setQueryData(queryKeys.notificationPreferences, data);
    },
  });
}
