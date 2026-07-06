/** A notification type known to the backend (drives labels + preferences). */
export type NotificationType =
  | "STAGE_CHANGED"
  | "ANALYSIS_COMPLETED"
  | "PREFLIGHT_COMPLETED"
  | "QA_SIGNED_OFF";

/** A single notification feed item. */
export interface Notification {
  id: string;
  type: string;
  title: string;
  body?: string | null;
  projectId?: string | null;
  relatedEntityType?: string | null;
  relatedEntityId?: string | null;
  read: boolean;
  readAt?: string | null;
  sentAt?: string | null;
  createdAt: string;
}

/** The unread-count badge payload. */
export interface UnreadCount {
  count: number;
}

/** A user's effective channel preference for one notification type. */
export interface NotificationPreference {
  type: string;
  label: string;
  inAppEnabled: boolean;
  emailEnabled: boolean;
}
