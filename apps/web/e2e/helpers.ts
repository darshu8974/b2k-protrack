import { expect, type Page } from "@playwright/test";

export type Role = "PM" | "DESIGNER" | "QA" | "ADMIN";

/** Seeded demo users (V2 identity migration). All share the password "password". */
export const USERS: Record<Role, { email: string; name: string }> = {
  PM: { email: "priya.anand@protrack.io", name: "Priya Anand" },
  DESIGNER: { email: "marcus.reed@protrack.io", name: "Marcus Reed" },
  QA: { email: "lena.ortiz@protrack.io", name: "Lena Ortiz" },
  ADMIN: { email: "david.cho@protrack.io", name: "David Cho" },
};

export const PASSWORD = "password";

/** The nav labels each role should (and should not) see — the role-shaped navigation matrix. */
export const NAV_MATRIX: Record<Role, { present: string[]; absent: string[] }> = {
  PM: { present: ["Dashboard", "Projects", "Review queue", "Reports"], absent: ["Users & roles", "Audit log", "My tasks"] },
  DESIGNER: { present: ["Dashboard", "Projects", "My tasks", "Production"], absent: ["Reports", "Users & roles", "Audit log"] },
  QA: { present: ["Dashboard", "Projects", "QA queue", "PDF reviews", "Reports"], absent: ["Users & roles", "Audit log", "My tasks"] },
  ADMIN: { present: ["Dashboard", "Projects", "Users & roles", "Audit log", "Reports"], absent: ["My tasks", "QA queue"] },
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
