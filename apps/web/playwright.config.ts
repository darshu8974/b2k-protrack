import { defineConfig, devices } from "@playwright/test";

/**
 * Playwright E2E config. Drives the built React app against the real Spring API + Neon stack.
 *
 * The web dev server is started automatically (see `webServer`). The API must already be running
 * on :8080 (`cd apps/api && ./gradlew bootRun` with apps/api/.env sourced) — override the target
 * with E2E_BASE_URL / E2E_API_URL. Seeded demo users (all password "password") are the fixtures.
 */
const BASE_URL = process.env.E2E_BASE_URL ?? "http://localhost:5173";

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: process.env.CI ? [["github"], ["html", { open: "never" }]] : [["list"]],
  use: {
    baseURL: BASE_URL,
    trace: "on-first-retry",
    screenshot: "only-on-failure",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: {
    command: "npm run dev",
    url: BASE_URL,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
});
