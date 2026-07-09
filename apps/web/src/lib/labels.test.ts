import { describe, expect, it } from "vitest";

import {
  auditEventLabel,
  checkKeyLabel,
  CHECK_KEYS,
  docTypeLabel,
  metricLabel,
  notificationTypeLabel,
  STAGE_LABEL,
} from "./labels";

describe("label lookups", () => {
  it("maps known document types and falls back to the raw code", () => {
    expect(docTypeLabel("MANUSCRIPT")).toBe("Manuscript");
    expect(docTypeLabel("PRODUCTION_PDF")).toBe("Production PDF");
    expect(docTypeLabel("SOMETHING_NEW")).toBe("SOMETHING_NEW");
  });

  it("maps known preflight check keys and falls back to the raw key", () => {
    expect(checkKeyLabel("geometry")).toBe("Page geometry");
    expect(checkKeyLabel("font_embedding")).toBe("Font embedding");
    expect(checkKeyLabel("unknown_check")).toBe("unknown_check");
  });

  it("maps known metrics and audit events, falling back otherwise", () => {
    expect(metricLabel("pages")).toBe("Pages");
    expect(metricLabel("mystery")).toBe("mystery");
    expect(auditEventLabel("QA_SIGNED_OFF")).toBe("QA sign-off");
    expect(auditEventLabel("BRAND_NEW_EVENT")).toBe("BRAND_NEW_EVENT");
  });

  it("maps notification types with a fallback to the raw type", () => {
    expect(notificationTypeLabel("STAGE_CHANGED")).toBe("Workflow");
    expect(notificationTypeLabel("UNMAPPED")).toBe("UNMAPPED");
  });
});

describe("preflight check ordering", () => {
  it("lists the six Phase-1 checks and every key has a label", () => {
    expect(CHECK_KEYS).toHaveLength(6);
    for (const key of CHECK_KEYS) {
      expect(checkKeyLabel(key)).not.toBe(key);
    }
  });
});

describe("stage labels", () => {
  it("covers all seven workflow stages", () => {
    expect(Object.keys(STAGE_LABEL)).toHaveLength(7);
    expect(STAGE_LABEL.QA_SIGNOFF).toBe("QA Sign-off");
  });
});
