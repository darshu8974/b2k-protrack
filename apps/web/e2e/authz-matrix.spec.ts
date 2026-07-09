import { expect, test } from "@playwright/test";

import { login, type Role } from "./helpers";

/**
 * Authorization matrix (UI enforcement of RBAC). ADMIN-only routes must render their content for
 * ADMIN and the 403 Forbidden page for every other role. The backend @PreAuthorize remains the
 * real gate — this asserts the frontend RoleRoute mirrors it so non-admins never see admin data.
 */
const ADMIN_ROUTES = [
  { path: "/admin/users", heading: "Users & roles" },
  { path: "/admin/audit", heading: "Audit log" },
];

const NON_ADMIN: Role[] = ["PM", "DESIGNER", "QA"];

test.describe("admin route authorization", () => {
  for (const route of ADMIN_ROUTES) {
    test(`ADMIN can open ${route.path}`, async ({ page }) => {
      await login(page, "ADMIN");
      await page.goto(route.path);
      await expect(page.getByRole("heading", { name: route.heading })).toBeVisible();
      await expect(page.getByText("You don't have permission", { exact: false })).toHaveCount(0);
    });

    for (const role of NON_ADMIN) {
      test(`${role} is forbidden from ${route.path}`, async ({ page }) => {
        await login(page, role);
        await page.goto(route.path);
        await expect(page.getByRole("heading", { name: "403" })).toBeVisible();
        await expect(page.getByText("You don't have permission", { exact: false })).toBeVisible();
        // Admin content must not leak.
        await expect(page.getByRole("heading", { name: route.heading })).toHaveCount(0);
      });
    }
  }
});
