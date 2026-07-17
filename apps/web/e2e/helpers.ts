import { expect, type Page } from "@playwright/test";

export type Role = "PROJECT_MANAGER" | "PAGINATOR" | "QC" | "QA" | "ADMIN";

/** Seeded demo users (V13 role-hierarchy migration). All share the password "password". */
export const USERS: Record<Role, { email: string; name: string }> = {
  PROJECT_MANAGER: { email: "pm@protrack.io", name: "Project Manager" },
  PAGINATOR: { email: "paginator@protrack.io", name: "Paginator" },
  QC: { email: "qc@protrack.io", name: "QC Reviewer" },
  QA: { email: "qa@protrack.io", name: "QA Approver" },
  ADMIN: { email: "admin@protrack.io", name: "Admin User" },
};

export const PASSWORD = "password";

/** The nav labels each role should (and should not) see — the role-shaped navigation matrix. */
export const NAV_MATRIX: Record<Role, { present: string[]; absent: string[] }> = {
  PROJECT_MANAGER: { present: ["Dashboard", "Projects", "Review queue", "Reports"], absent: ["Users & roles", "Audit log", "My tasks"] },
  PAGINATOR: { present: ["Dashboard", "Projects", "My tasks", "Production"], absent: ["Reports", "Users & roles", "Audit log"] },
  QC: { present: ["Dashboard", "Projects", "QC queue", "PDF reviews", "Reports"], absent: ["Users & roles", "Audit log", "My tasks"] },
  QA: { present: ["Dashboard", "Projects", "Sign-off queue", "PDF reviews", "Reports"], absent: ["Users & roles", "Audit log", "My tasks"] },
  ADMIN: { present: ["Dashboard", "Projects", "Users & roles", "Audit log", "Reports"], absent: ["My tasks", "QC queue"] },
};

/** Log in through the real login form as the given role and land on the dashboard. */
export async function login(page: Page, role: Role): Promise<void> {
  const { email } = USERS[role];
  await page.goto("/login");
  await page.getByLabel(/work email/i).fill(email);
  await page.getByLabel(/password/i).fill(PASSWORD);
  await page.getByRole("button", { name: /sign in/i }).click();
  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole("heading", { name: /good morning/i })).toBeVisible();
}

/** The main-navigation landmark (Sidebar). */
export function nav(page: Page) {
  return page.getByRole("navigation", { name: /main navigation/i });
}
