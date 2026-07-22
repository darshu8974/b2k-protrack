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
 * Role-shaped navigation. Only items wired to a real route are listed — the AI Assistant lives
 * inside each project's "Assistant" tab rather than as a standalone page, so it isn't a top-level
 * nav item. Reports is restricted to ADMIN / PROJECT_MANAGER / QC / QA (paginators are excluded
 * per the API spec), so it is omitted from the paginator section rather than shown as a forbidden
 * link.
 */
export const navConfig: Record<Role, NavSection[]> = {
  PROJECT_MANAGER: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard", icon: "dashboard" },
        { label: "Projects", path: "/projects", icon: "folder" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [{ label: "Reports", path: "/reports", icon: "reports" }],
    },
  ],
  PAGINATOR: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard", icon: "dashboard" },
        { label: "Projects", path: "/projects", icon: "folder" },
      ],
    },
  ],
  QC: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard", icon: "dashboard" },
        { label: "Projects", path: "/projects", icon: "folder" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [{ label: "Reports", path: "/reports", icon: "reports" }],
    },
  ],
  QA: [
    {
      group: "WORKSPACE",
      items: [
        { label: "Dashboard", path: "/dashboard", icon: "dashboard" },
        { label: "Projects", path: "/projects", icon: "folder" },
      ],
    },
    {
      group: "INTELLIGENCE",
      items: [{ label: "Reports", path: "/reports", icon: "reports" }],
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
      items: [{ label: "Reports", path: "/reports", icon: "reports" }],
    },
  ],
};
