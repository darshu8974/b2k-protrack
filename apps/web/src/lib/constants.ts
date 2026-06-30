import type { Role } from "../types/domain";

/** Human-readable labels for the four roles. */
export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: "Administrator",
  PM: "Project Manager",
  DESIGNER: "Production Designer",
  QA: "QA Engineer",
};

/** Avatar background colors per role (placeholder seed styling). */
export const ROLE_COLORS: Record<Role, string> = {
  ADMIN: "#6D5EF0",
  PM: "#0B63CE",
  DESIGNER: "#1F9D57",
  QA: "#C9821A",
};
