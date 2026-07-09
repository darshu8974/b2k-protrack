import { describe, expect, it } from "vitest";

import type { Role } from "../types/domain";
import { navConfig } from "./navConfig";

const ALL_ROLES: Role[] = ["ADMIN", "PM", "DESIGNER", "QA"];

function labels(role: Role): string[] {
  return navConfig[role].flatMap((section) => section.items.map((item) => item.label));
}

describe("role-shaped navigation", () => {
  it("defines a nav model for every role", () => {
    for (const role of ALL_ROLES) {
      expect(navConfig[role].length).toBeGreaterThan(0);
    }
  });

  it("gives every role Dashboard and Projects", () => {
    for (const role of ALL_ROLES) {
      expect(labels(role)).toEqual(expect.arrayContaining(["Dashboard", "Projects"]));
    }
  });

  it("excludes Reports from designers but includes it for PM/QA/ADMIN", () => {
    expect(labels("DESIGNER")).not.toContain("Reports");
    expect(labels("PM")).toContain("Reports");
    expect(labels("QA")).toContain("Reports");
    expect(labels("ADMIN")).toContain("Reports");
  });

  it("exposes admin-only destinations to ADMIN and points them at real routes", () => {
    const adminItems = navConfig.ADMIN.flatMap((s) => s.items);
    const usersItem = adminItems.find((i) => i.label === "Users & roles");
    const auditItem = adminItems.find((i) => i.label === "Audit log");
    expect(usersItem?.path).toBe("/admin/users");
    expect(auditItem?.path).toBe("/admin/audit");

    expect(labels("PM")).not.toContain("Users & roles");
    expect(labels("PM")).not.toContain("Audit log");
  });
});
