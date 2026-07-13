import type { Role } from "../types/domain";

export interface NavItem {
  label: string;
  /** Placeholder target until the real feature routes land in later sprints. */
  path: string;
  /** Icon key resolved to a Material icon in the Sidebar (presentational only). */
  icon: string;
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
        { label: "Dashboard", path: "/dashboard", icon: "dashboard" },
        { label: "Projects", path: "/projects", icon: "folder" },
        { label: "Review queue", path: "/health", icon: "review" },
        { label: "Team", path: "/health", icon: "team" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [
        { label: "AI Assistant", path: "/health", icon: "assistant" },
        { label: "Reports", path: "/reports", icon: "reports" },
      ],
    },
  ],
  DESIGNER: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard", icon: "dashboard" },
        { label: "My tasks", path: "/health", icon: "tasks" },
        { label: "Projects", path: "/projects", icon: "folder" },
        { label: "Production", path: "/health", icon: "production" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [{ label: "AI Assistant", path: "/health", icon: "assistant" }],
    },
  ],
  QA: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard", icon: "dashboard" },
        { label: "QA queue", path: "/health", icon: "qa" },
        { label: "PDF reviews", path: "/health", icon: "review" },
        { label: "Projects", path: "/projects", icon: "folder" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [
        { label: "AI Assistant", path: "/health", icon: "assistant" },
        { label: "Reports", path: "/reports", icon: "reports" },
      ],
    },
  ],
  ADMIN: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard", icon: "dashboard" },
        { label: "Projects", path: "/projects", icon: "folder" },
        { label: "Users & roles", path: "/admin/users", icon: "users" },
        { label: "Audit log", path: "/admin/audit", icon: "audit" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [
        { label: "AI Assistant", path: "/health", icon: "assistant" },
        { label: "Reports", path: "/reports", icon: "reports" },
      ],
    },
  ],
};
