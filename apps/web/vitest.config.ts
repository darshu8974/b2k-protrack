import react from "@vitejs/plugin-react";
import { defineConfig } from "vitest/config";

// Vitest configuration kept separate from vite.config.ts so the production build stays free of
// test-only settings. Component tests run in jsdom; setup registers jest-dom matchers and RTL
// cleanup. Tests import the vitest API explicitly (globals off) to stay lint-clean.
export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    globals: false,
    setupFiles: ["./src/test/setup.ts"],
    include: ["src/**/*.test.{ts,tsx}"],
    css: false,
    restoreMocks: true,
  },
});
