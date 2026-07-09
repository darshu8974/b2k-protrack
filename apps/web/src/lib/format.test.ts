import { afterEach, describe, expect, it, vi } from "vitest";

import { formatBytes, formatRelativeTime } from "./format";

describe("formatBytes", () => {
  it("renders an em dash for null/undefined", () => {
    expect(formatBytes(null)).toBe("—");
    expect(formatBytes(undefined)).toBe("—");
  });

  it("renders zero without decimals", () => {
    expect(formatBytes(0)).toBe("0 B");
  });

  it("keeps whole bytes without a decimal", () => {
    expect(formatBytes(512)).toBe("512 B");
  });

  it("scales into KB/MB/GB with one decimal", () => {
    expect(formatBytes(1536)).toBe("1.5 KB");
    expect(formatBytes(1024 * 1024)).toBe("1 MB");
    expect(formatBytes(1024 * 1024 * 1024 * 2.5)).toBe("2.5 GB");
  });

  it("caps at the largest known unit (TB)", () => {
    expect(formatBytes(1024 ** 5)).toBe("1024 TB");
  });
});

describe("formatRelativeTime", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  function freezeAt(iso: string) {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(iso));
  }

  it("renders an em dash for missing or unparseable input", () => {
    expect(formatRelativeTime(null)).toBe("—");
    expect(formatRelativeTime(undefined)).toBe("—");
    expect(formatRelativeTime("not-a-date")).toBe("—");
  });

  it("renders 'just now' within 45 seconds", () => {
    freezeAt("2026-01-01T12:00:30Z");
    expect(formatRelativeTime("2026-01-01T12:00:00Z")).toBe("just now");
  });

  it("renders minutes, hours and days below the week threshold", () => {
    freezeAt("2026-01-01T12:00:00Z");
    expect(formatRelativeTime("2026-01-01T11:30:00Z")).toBe("30m ago");
    expect(formatRelativeTime("2026-01-01T09:00:00Z")).toBe("3h ago");
    expect(formatRelativeTime("2025-12-30T12:00:00Z")).toBe("2d ago");
  });

  it("falls back to a locale date beyond a week", () => {
    freezeAt("2026-01-15T12:00:00Z");
    const result = formatRelativeTime("2026-01-01T12:00:00Z");
    expect(result).not.toContain("ago");
    expect(result).toBe(new Date("2026-01-01T12:00:00Z").toLocaleDateString());
  });
});
