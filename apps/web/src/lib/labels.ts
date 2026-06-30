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
