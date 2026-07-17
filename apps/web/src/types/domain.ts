/** The five fixed roles in the Sprint-8 hierarchy. */
export type Role = "ADMIN" | "PROJECT_MANAGER" | "PAGINATOR" | "QC" | "QA";

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
