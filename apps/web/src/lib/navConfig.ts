import type { Role } from "../types/domain";

export interface NavItem {
  label: string;
  /** Placeholder target until the real feature routes land in later sprints. */
  path: string;
}

export interface NavSection {
  group: string;
  items: NavItem[];
}

/**
 * Role-shaped navigation. Item paths currently point at /health (the Sprint-0 placeholder);
 * they are repointed to real routes as each feature is built. Reports is restricted to
 * ADMIN / PM / QA (designers are excluded per the API spec), so it is omitted from the
 * designer section rather than shown as a forbidden link.
 */
export const navConfig: Record<Role, NavSection[]> = {
  PM: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard" },
        { label: "Projects", path: "/projects" },
        { label: "Review queue", path: "/health" },
        { label: "Team", path: "/health" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [
        { label: "AI Assistant", path: "/health" },
        { label: "Reports", path: "/reports" },
      ],
    },
  ],
  DESIGNER: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard" },
        { label: "My tasks", path: "/health" },
        { label: "Projects", path: "/projects" },
        { label: "Production", path: "/health" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [{ label: "AI Assistant", path: "/health" }],
    },
  ],
  QA: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard" },
        { label: "QA queue", path: "/health" },
        { label: "PDF reviews", path: "/health" },
        { label: "Projects", path: "/projects" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [
        { label: "AI Assistant", path: "/health" },
        { label: "Reports", path: "/reports" },
      ],
    },
  ],
  ADMIN: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard" },
        { label: "Projects", path: "/projects" },
        { label: "Users & roles", path: "/admin/users" },
        { label: "Audit log", path: "/admin/audit" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [
        { label: "AI Assistant", path: "/health" },
        { label: "Reports", path: "/reports" },
      ],
    },
  ],
};
