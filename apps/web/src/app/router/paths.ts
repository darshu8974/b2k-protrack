/** Typed route builders. Feature paths are added here as features land. */
export const paths = {
  login: "/login",
  dashboard: "/dashboard",
  projects: "/projects",
  projectNew: "/projects/new",
  project: (id: string) => `/projects/${id}`,
  adminUsers: "/admin/users",
  adminAudit: "/admin/audit",
  health: "/health",
} as const;
