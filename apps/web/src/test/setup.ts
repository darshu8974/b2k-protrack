// Global test setup: register @testing-library/jest-dom matchers for Vitest's expect and unmount
// React trees after each test so components don't leak state across cases.
import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach } from "vitest";

afterEach(() => {
  cleanup();
});
