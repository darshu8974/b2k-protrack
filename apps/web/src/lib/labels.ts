import type { DocType } from "../types/files";
import type { PackageStatus } from "../types/package";
import type { Priority, ProjectStatus, PublicationType, Stage } from "../types/project";

type ChipColor = "default" | "primary" | "secondary" | "success" | "warning" | "error" | "info";

export const STAGE_LABEL: Record<Stage, string> = {
  INTAKE: "Intake",
  AI_ANALYSIS: "AI Analysis",
  DESIGN_PREP: "Design Prep",
  IN_PRODUCTION: "In Production",
  PDF_REVIEW: "PDF Review",
  QA_SIGNOFF: "QA Sign-off",
  COMPLETED: "Completed",
};

export const STATUS_LABEL: Record<ProjectStatus, string> = {
  ACTIVE: "Active",
  ON_HOLD: "On Hold",
  COMPLETED: "Completed",
  ARCHIVED: "Archived",
};

export const STATUS_COLOR: Record<ProjectStatus, ChipColor> = {
  ACTIVE: "info",
  ON_HOLD: "warning",
  COMPLETED: "success",
  ARCHIVED: "default",
};

export const PRIORITY_LABEL: Record<Priority, string> = {
  LOW: "Low",
  MEDIUM: "Medium",
  HIGH: "High",
};

export const PRIORITY_COLOR: Record<Priority, ChipColor> = {
  LOW: "default",
  MEDIUM: "primary",
  HIGH: "error",
};

export const PUBLICATION_TYPE_LABEL: Record<PublicationType, string> = {
  STEM_TEXTBOOK: "STEM Textbook",
  MONOGRAPH: "Monograph",
  JOURNAL: "Journal",
  REFERENCE: "Reference",
};

export const DOC_TYPE_LABEL: Record<DocType, string> = {
  MANUSCRIPT: "Manuscript",
  PRODUCTION_PDF: "Production PDF",
  STRUCTURED_XML: "Structured XML",
  FIGURES_MANIFEST: "Figures manifest",
  OTHER: "Other",
};

export function docTypeLabel(docType: string): string {
  return DOC_TYPE_LABEL[docType as DocType] ?? docType;
}

export const PACKAGE_STATUS_LABEL: Record<PackageStatus, string> = {
  DRAFT: "Draft",
  ASSEMBLING: "Assembling",
  ASSEMBLED: "Assembled",
  FAILED: "Failed",
};

export const PACKAGE_STATUS_COLOR: Record<PackageStatus, ChipColor> = {
  DRAFT: "default",
  ASSEMBLING: "warning",
  ASSEMBLED: "success",
  FAILED: "error",
};

export const AUDIT_EVENT_LABEL: Record<string, string> = {
  PROJECT_CREATED: "Project created",
  PROJECT_UPDATED: "Project updated",
  MEMBERS_ASSIGNED: "Members assigned",
  STAGE_CHANGED: "Stage changed",
  FILE_UPLOADED: "File uploaded",
  PACKAGE_ASSEMBLED: "Package assembled",
  ANALYSIS_STARTED: "AI analysis started",
  ANALYSIS_COMPLETED: "AI analysis completed",
  ANALYSIS_FAILED: "AI job failed",
  PREFLIGHT_STARTED: "Preflight started",
  PREFLIGHT_COMPLETED: "Preflight completed",
  PREFLIGHT_FAILED: "Preflight failed",
  ISSUE_DECIDED: "QA issue decided",
  QA_SIGNED_OFF: "QA sign-off",
};

export const CHECK_KEY_LABEL: Record<string, string> = {
  geometry: "Page geometry",
  font_embedding: "Font embedding",
  image_resolution: "Image resolution",
  overflow: "Content overflow",
  placement: "Safe-area placement",
  accessibility: "Accessibility",
};

export function checkKeyLabel(key: string): string {
  return CHECK_KEY_LABEL[key] ?? key;
}

/** Fixed order of the Phase-1 preflight checks (matches the backend registry). */
export const CHECK_KEYS = [
  "geometry",
  "font_embedding",
  "image_resolution",
  "overflow",
  "placement",
  "accessibility",
] as const;

export const CHECK_RESULT_COLOR: Record<string, ChipColor> = {
  PASS: "success",
  REVIEW: "warning",
  FAIL: "error",
};

export const ISSUE_STATUS_LABEL: Record<string, string> = {
  OPEN: "Open",
  TRIAGED: "Triaged",
  RESOLVED: "Resolved",
  WAIVED: "Waived",
};

export const ISSUE_STATUS_COLOR: Record<string, ChipColor> = {
  OPEN: "warning",
  TRIAGED: "info",
  RESOLVED: "success",
  WAIVED: "default",
};

export const DECISION_LABEL: Record<string, string> = {
  ACCEPT_FIX: "Accept fix",
  SEND_BACK: "Send back",
  COMMENT: "Comment",
};

export const SEVERITY_LABEL: Record<string, string> = {
  HIGH: "High",
  MEDIUM: "Medium",
  LOW: "Low",
};

export const SEVERITY_COLOR: Record<string, ChipColor> = {
  HIGH: "error",
  MEDIUM: "warning",
  LOW: "default",
};

export const METRIC_LABEL: Record<string, string> = {
  pages: "Pages",
  figures: "Figures",
  tables: "Tables",
  equations: "Equations",
  problems: "Problems",
  references: "References",
};

export function metricLabel(key: string): string {
  return METRIC_LABEL[key] ?? key;
}

export function auditEventLabel(eventType: string): string {
  return AUDIT_EVENT_LABEL[eventType] ?? eventType;
}
