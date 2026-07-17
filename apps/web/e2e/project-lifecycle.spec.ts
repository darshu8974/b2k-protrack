import { expect, test } from "@playwright/test";

import { login } from "./helpers";

/**
 * Core pipeline entry: a PM creates a project through the wizard, lands on its details page, and
 * finds it in the projects list. NOTE: there is no project-delete endpoint, so each run appends a
 * row — CI should target an ephemeral/branch database. Titles are prefixed "E2E Playwright" so
 * fixtures are identifiable and cleanable.
 */
test.describe("project lifecycle (PM)", () => {
  test("PM creates a project and it appears in the list", async ({ page }) => {
    await login(page, "PROJECT_MANAGER");

    const title = `E2E Playwright ${Date.now()}`;

    await page.goto("/projects/new");
    await page.getByLabel(/publication title/i).fill(title);

    // MUI select: open the Imprint dropdown and pick the seeded imprint.
    await page.getByLabel("Imprint").click();
    await page.getByRole("option", { name: "Physical Sciences" }).click();

    await page.getByRole("button", { name: /create project/i }).click();

    // Redirected to the new project's details page (UUID route) with its title as the heading.
    await expect(page).toHaveURL(/\/projects\/[0-9a-f-]{36}$/);
    await expect(page.getByRole("heading", { name: title })).toBeVisible();
    // A fresh project starts at the Intake stage.
    await expect(page.getByText("Intake", { exact: true }).first()).toBeVisible();

    // And it is discoverable in the projects list via search.
    await page.goto("/projects");
    await page.getByLabel(/search/i).fill(title);
    await expect(page.getByRole("cell", { name: title })).toBeVisible();
  });

  test("non-PM roles cannot open the create-project route", async ({ page }) => {
    await login(page, "PAGINATOR");
    await page.goto("/projects/new");
    await expect(page.getByRole("heading", { name: "403" })).toBeVisible();
  });
});
