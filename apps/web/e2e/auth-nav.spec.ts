import { expect, test } from "@playwright/test";

import { login, nav, NAV_MATRIX, PASSWORD, USERS, type Role } from "./helpers";

const ROLES: Role[] = ["PROJECT_MANAGER", "PAGINATOR", "QC", "QA", "ADMIN"];

test.describe("authentication & role-shaped navigation", () => {
  for (const role of ROLES) {
    test(`${role} logs in and sees the correct navigation`, async ({ page }) => {
      await login(page, role);

      for (const label of NAV_MATRIX[role].present) {
        await expect(nav(page).getByText(label, { exact: true })).toBeVisible();
      }
      for (const label of NAV_MATRIX[role].absent) {
        await expect(nav(page).getByText(label, { exact: true })).toHaveCount(0);
      }
    });
  }

  test("rejects invalid credentials with an error alert", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel(/work email/i).fill(USERS.PROJECT_MANAGER.email);
    await page.getByLabel(/password/i).fill("wrong-password");
    await page.getByRole("button", { name: /sign in/i }).click();

    await expect(page.getByRole("alert")).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });

  test("redirects unauthenticated users away from a protected route", async ({ page }) => {
    await page.goto("/projects");
    await expect(page).toHaveURL(/\/login$/);
  });

  test("logout clears the session and returns to login", async ({ page }) => {
    await login(page, "PROJECT_MANAGER");
    await page.getByRole("button", { name: /account/i }).click();
    await page.getByRole("menuitem", { name: /sign out/i }).click();
    await expect(page).toHaveURL(/\/login$/);

    // Session is gone: a protected route bounces back to login.
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login$/);
  });

  test("keeps the session across a reload (refresh-token bootstrap)", async ({ page }) => {
    await login(page, "QA");
    await page.reload();
    await expect(page.getByRole("heading", { name: /good morning/i })).toBeVisible();
    await expect(nav(page).getByText("Sign-off queue", { exact: true })).toBeVisible();
  });
});

test("login form prefills the admin demo account", async ({ page }) => {
  await page.goto("/login");
  await expect(page.getByLabel(/work email/i)).toHaveValue(USERS.ADMIN.email);
  await expect(page.getByLabel(/password/i)).toHaveValue(PASSWORD);
});
