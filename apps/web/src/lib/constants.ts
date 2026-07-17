import type { Role } from "../types/domain";

/** Human-readable labels for the five roles. */
export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: "Administrator",
  PROJECT_MANAGER: "Project Manager",
  PAGINATOR: "Paginator",
  QC: "QC",
  QA: "QA",
};

/** Avatar background colors per role (placeholder seed styling). */
export const ROLE_COLORS: Record<Role, string> = {
  ADMIN: "#6D5EF0",
  PROJECT_MANAGER: "#0B63CE",
  PAGINATOR: "#1F9D57",
  QC: "#0891B2",
  QA: "#C9821A",
};
