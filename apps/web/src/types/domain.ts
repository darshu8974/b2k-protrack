/** The four fixed roles (sample user names are placeholder data only). */
export type Role = "ADMIN" | "PM" | "DESIGNER" | "QA";

/** Authenticated user summary, sourced from /me in Sprint 1. */
export interface UserSummary {
  id: string;
  fullName: string;
  email: string;
  roles: Role[];
  permissions: string[];
  avatarInitials: string;
  avatarColor?: string;
}
