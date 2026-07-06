import { useQuery } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import { getUnreadCount } from "./api";

/**
 * The unread-notification count for the TopBar bell badge. A small standalone query (not context),
 * polled so the badge stays fresh without a live socket; also refetches on window focus.
 */
export function useUnreadCount() {
  return useQuery({
    queryKey: queryKeys.notificationsUnreadCount,
    queryFn: getUnreadCount,
    refetchInterval: 30_000,
    refetchOnWindowFocus: true,
    staleTime: 15_000,
  });
}
