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
};

export function auditEventLabel(eventType: string): string {
  return AUDIT_EVENT_LABEL[eventType] ?? eventType;
}
